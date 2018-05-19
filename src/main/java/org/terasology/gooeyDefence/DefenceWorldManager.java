/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gooeyDefence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.flexiblepathfinding.PathfinderSystem;
import org.terasology.gooeyDefence.components.ShrineComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.OnChangedBlock;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class that provides dynamic information about the Defence Field.
 * Also performs all high level actions, delegating specifics to other systems.
 * Static information is given by {@link DefenceField}
 *
 * @see DefenceField
 */
@Share(DefenceWorldManager.class)
@RegisterSystem
public class DefenceWorldManager extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(DefenceWorldManager.class);

    private List<List<Vector3i>> paths = new ArrayList<>(Collections.nCopies(DefenceField.entranceCount(), null));

    @In
    private PathfinderSystem pathfinderSystem;
    @In
    private BlockManager blockManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;

    private EntityRef shrineEntity;

    private boolean fieldActivated;

    private Block airBlock;

    @Override
    public void initialise() {
        airBlock = blockManager.getBlock(BlockManager.AIR_ID);
    }

    /**
     * Implemented to ensure we capture both manual and automatic saves
     */
    @Override
    public void preAutoSave() {
        preSave();
    }

    @Override
    public void preSave() {
        if (fieldActivated) {
            ShrineComponent component = shrineEntity.getComponent(ShrineComponent.class);
            component.setPaths(paths);
            component.setSaved(true);
        }
    }

    @ReceiveEvent
    public void onChangedBlock(OnChangedBlock event, EntityRef entity) {
        if (event.getNewType() == airBlock || event.getOldType() == airBlock) {
            calculatePaths();
        }
    }

    @ReceiveEvent
    public void onActivate(ActivateEvent event, EntityRef entity, ShrineComponent component) {
        if (!fieldActivated) {
            setupWorld();
        }
    }

    /**
     * Initialises the defence field
     */
    public void setupWorld() {
        logger.info("Setting up the world.");
        fieldActivated = true;

        shrineEntity = blockEntityRegistry.getBlockEntityAt(DefenceField.getShrineBlock());
        ShrineComponent component = shrineEntity.getComponent(ShrineComponent.class);
        if (component.isSaved()) {
            logger.info("Attempting to retrieve saved data");
            paths = component.getPaths();
        }

        calculatePaths();
    }

    /**
     * Calculate the path from an entrance to the centre
     *
     * @param id The entrance to calculate from
     */
    public void calculatePath(int id) {
        pathfinderSystem.requestPath(
                DefenceField.entrancePos(id), DefenceField.fieldCentre(), (path, target) -> paths.set(id, path));
    }

    /**
     * Calculate paths from all the entrances to the centre.
     */
    public void calculatePaths() {
        for (int id = 0; id < DefenceField.entranceCount(); id++) {
            calculatePath(id);
        }
    }

    /**
     * @return All paths from entrance to centre
     */
    public List<List<Vector3i>> getPaths() {
        return paths;
    }
}
