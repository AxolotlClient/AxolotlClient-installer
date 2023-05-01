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

package io.github.axolotlclient.installer.util;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// i'm sorry random stackoverflow person
// also thanks <3
// https://stackoverflow.com/a/39390467
public interface SimpleDocumentListener extends DocumentListener {

    void update(DocumentEvent event);

    @Override
    default void insertUpdate(DocumentEvent event) {
        update(event);
    }

    @Override
    default void removeUpdate(DocumentEvent event) {
        update(event);
    }

    @Override
    default void changedUpdate(DocumentEvent event) {
        update(event);
    }

}
