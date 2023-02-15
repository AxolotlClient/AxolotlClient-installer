/*
 * Copyright © 2023-2023 moehreag <moehreag@gmail.com>, TheKodeToad <TheKodeToad@proton.me> & Contributors
 *
 * This file is part of AxolotlClient Installer.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.installer;

import javax.swing.JProgressBar;

public interface ProgressConsumer {

    static ProgressConsumer of(JProgressBar bar) {
        return (string, progress) -> {
            if (progress == -1) {
                bar.setValue(0);
                bar.setIndeterminate(true);
            } else {
                bar.setValue((int) (bar.getMaximum() * progress));
                bar.setIndeterminate(false);
            }

            if (string != null)
                bar.setString(string);
        };
    }

    default ProgressConsumer subprogress(float start, float end) {
        return (string, progress) -> update(string, start + (end - start) * progress);
    }

    default void update(float progress) {
        update(null, progress);
    }

    /**
     * Updates the progress.
     * @param string <code>null</code> to keep the last message.
     * @param progress the progress.
     */
    void update(String string, float progress);
}
