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

import static io.github.axolotlclient.installer.util.Translate.tr;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.formdev.flatlaf.FlatLightLaf;

import io.github.axolotlclient.installer.util.Util;

/**
 * UI for installer.
 */
public final class InstallerApp {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 300;
    private static final int COMBO_WIDTH = 100;
    private static final int PROGRESS_WIDTH = 200;

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, UnsupportedLookAndFeelException, InterruptedException, ExecutionException {
        try {
            FlatLightLaf.setup();

            Installer installer = new Installer();

            // create a frame and use it for installation
            JFrame frame = new JFrame(tr("title"));
            frame.setIconImage(ImageIO.read(Objects.requireNonNull(InstallerApp.class.getResourceAsStream("/icon.png"))));
            frame.setLayout(null);

            // add components
            JLabel heading = new JLabel(tr("heading"));
            heading.setFont(heading.getFont().deriveFont(Font.BOLD, 30));

            heading.setBounds(WIDTH / 2 - heading.getPreferredSize().width / 2, 14, heading.getPreferredSize().width,
                    heading.getPreferredSize().height);
            frame.add(heading);

            JLabel minecraftVersionLabel = new JLabel(tr("minecraft_version"));
            JComboBox<?> minecraftVersionBox = new JComboBox<>(
                    new DefaultComboBoxModel<>(installer.getAvailableGameVers().toArray(new String[0])));

            minecraftVersionLabel.setBounds(WIDTH / 2 - minecraftVersionLabel.getPreferredSize().width / 2, 105,
                    minecraftVersionLabel.getPreferredSize().width, minecraftVersionLabel.getPreferredSize().height);
            minecraftVersionBox.setBounds(WIDTH / 2 - COMBO_WIDTH / 2,
                    minecraftVersionLabel.getY() + minecraftVersionLabel.getHeight(), COMBO_WIDTH,
                    minecraftVersionBox.getPreferredSize().height);

            frame.add(minecraftVersionLabel);
            frame.add(minecraftVersionBox);

            JProgressBar progress = new JProgressBar(0, 100);
            progress.setBounds(WIDTH / 2 - PROGRESS_WIDTH / 2, HEIGHT - 110, PROGRESS_WIDTH, 20);
            progress.setVisible(false);
            progress.setStringPainted(true);

            frame.add(progress);

            JButton installButton = new JButton(tr("install"));
            installButton.setBounds(WIDTH / 2 - installButton.getPreferredSize().width / 2, HEIGHT - 80,
                    installButton.getPreferredSize().width, installButton.getPreferredSize().height);
            installButton.addActionListener((event) -> {
                installButton.setEnabled(false);
                progress.setValue(0);
                progress.setString("");
                progress.setVisible(true);
                new Thread(() -> {
                    try {
                        installer.install(
                                installer.getModVerForGameVer(minecraftVersionBox.getSelectedItem().toString()),
                                Util.getDotMinecraft(), ProgressConsumer.of(progress));
                    } catch (Throwable e) {
                        System.err.println("Couldn't install");
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(frame, e.toString(), tr("install_error"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                    progress.setVisible(false);
                    installButton.setEnabled(true);
                }).start();
            });

            frame.add(installButton);

            // show
            frame.setSize(WIDTH, HEIGHT);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Throwable e) {
            System.err.println("Couldn't open the installer window");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.toString(), tr("open_error"), JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
