/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.gooeyDefence.ui.control;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.gooeyDefence.towerBlocks.base.TowerTargeter;
import org.terasology.gooeyDefence.waves.WaveGenerator;
import org.terasology.gooeyDefence.waves.WaveManager;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.widgets.UIButton;

/**
 * Allows the player to control information about the field.
 * This includes for instance, starting a wave.
 */
public class ControlScreen extends CoreScreenLayer {

    @In
    private WaveManager waveManager;
    @In
    private WaveGenerator waveGenerator;

    private UIButton startButton;
    private UIWaveInfo waveInfo;

    @Override

    public void initialise() {
        startButton = find("startButton", UIButton.class);
        waveInfo = find("waveInfo", UIWaveInfo.class);


        startButton.subscribe(this::startButtonPressed);
        waveInfo.setWaveInfo(waveGenerator.getCurrentWave());
    }

    private void startButtonPressed(UIWidget ignored) {
        waveManager.startAttack(waveGenerator.getCurrentWave());
        waveInfo.setWaveInfo(waveGenerator.generateWave());
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
