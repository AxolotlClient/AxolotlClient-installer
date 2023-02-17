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

import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.icons.FlatFileViewDirectoryIcon;

import io.github.axolotlclient.installer.util.DarkModeDetector;
import io.github.axolotlclient.installer.util.Util;

/**
 * UI for installer.
 */
public final class InstallerApp {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 300;
    private static final int PROGRESS_WIDTH = 200;

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, UnsupportedLookAndFeelException, InterruptedException, ExecutionException {
        try {
            if (DarkModeDetector.detect())
                FlatDarkLaf.setup();
            else
                FlatLightLaf.setup();

            Installer installer = new Installer();

            // create a frame and use it for installation
            JFrame frame = new JFrame(tr("title"));
            frame.setIconImage(
                    ImageIO.read(Objects.requireNonNull(InstallerApp.class.getResourceAsStream("/icon.png"))));
            frame.setLayout(null);

            // add components
            JLabel heading = new JLabel(tr("heading"));
            heading.setFont(heading.getFont().deriveFont(Font.BOLD, 30));

            heading.setBounds(WIDTH / 2 - heading.getPreferredSize().width / 2, 14, heading.getPreferredSize().width,
                    heading.getPreferredSize().height);
            frame.add(heading);

            JLabel minecraftVersionLabel = new JLabel(tr("minecraft_version"));
            JComboBox<String> minecraftVersionBox = new JComboBox<>(
                    new DefaultComboBoxModel<>(installer.getAvailableGameVers().toArray(new String[0])));

            minecraftVersionLabel.setBounds(WIDTH / 2 - minecraftVersionLabel.getPreferredSize().width / 2, 80,
                    minecraftVersionLabel.getPreferredSize().width, minecraftVersionLabel.getPreferredSize().height);
            minecraftVersionBox.setBounds(WIDTH / 2 - 50,
                    minecraftVersionLabel.getY() + minecraftVersionLabel.getHeight() + 3, 100,
                    minecraftVersionBox.getPreferredSize().height);

            frame.add(minecraftVersionLabel);
            frame.add(minecraftVersionBox);

            JLabel gameFolderLabel = new JLabel(tr("game_folder"));
            JTextField gameFolderBox = new JTextField();
            JButton gameFolderButton = new JButton(new FlatFileViewDirectoryIcon());

            gameFolderLabel.setBounds(WIDTH / 2 - gameFolderLabel.getPreferredSize().width / 2, 135,
                    gameFolderLabel.getPreferredSize().width, gameFolderLabel.getPreferredSize().height);
            gameFolderBox.setBounds(WIDTH / 2 - 100, gameFolderLabel.getY() + gameFolderLabel.getHeight() + 3,
                    200 - gameFolderBox.getPreferredSize().height - 5, gameFolderBox.getPreferredSize().height);
            gameFolderButton.setBounds(gameFolderBox.getX() + gameFolderBox.getWidth() + 5, gameFolderBox.getY(),
                    gameFolderBox.getHeight(), gameFolderBox.getHeight());

            boolean[] dirty = new boolean[1];
            String[] previousText = new String[1];

            Runnable update = () -> {
                gameFolderBox.setText(getGameFolder(minecraftVersionBox));
                previousText[0] = gameFolderBox.getText();
            };

            gameFolderBox.addFocusListener(new FocusAdapter() {

                @Override
                public void focusLost(FocusEvent event) {
                    if (!previousText[0].equals(gameFolderBox.getText()))
                        dirty[0] = true;
                }
            });
            minecraftVersionBox.addItemListener((event) -> {
                if (!dirty[0])
                    update.run();
            });
            update.run();

            gameFolderButton.addActionListener((event) -> {
                JFileChooser chooser = new JFileChooser(new File(gameFolderBox.getText()));
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
                    return;
                gameFolderBox.setText(chooser.getSelectedFile().getAbsolutePath());
                dirty[0] = true;
            });

            frame.add(gameFolderLabel);
            frame.add(gameFolderBox);
            frame.add(gameFolderButton);

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
                new Thread(() -> {
                    try {
                        Path gameDir = Paths.get(gameFolderBox.getText());

                        Path modsDir = gameDir.resolve("mods");
                        if (Files.isDirectory(modsDir) && (Files.list(modsDir).count() != 0)) {
                            int opt = JOptionPane.showConfirmDialog(frame, tr("mods_present"), tr("mods_present_title"),
                                    JOptionPane.YES_NO_CANCEL_OPTION);
                            if (opt == JOptionPane.CANCEL_OPTION) {
                                progress.setVisible(false);
                                installButton.setEnabled(true);
                                return;
                            } else if (opt == JOptionPane.YES_OPTION) {
                                Files.walkFileTree(modsDir, new SimpleFileVisitor<Path>() {

                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                            throws IOException {
                                        Files.delete(file);
                                        return FileVisitResult.CONTINUE;
                                    }

                                    @Override
                                    public FileVisitResult postVisitDirectory(Path file, IOException e)
                                            throws IOException {
                                        if (e != null)
                                            throw e;

                                        Files.delete(file);
                                        return FileVisitResult.CONTINUE;
                                    }
                                });
                            }
                        }

                        progress.setValue(0);
                        progress.setString("");
                        progress.setVisible(true);
                        installer.install(
                                installer.getModVerForGameVer(minecraftVersionBox.getSelectedItem().toString()),
                                Util.getDotMinecraft(), gameDir, ProgressConsumer.of(progress));
                    } catch (Throwable e) {
                        System.err.println("Couldn't install");
                        e.printStackTrace();
                        progress.setVisible(false);
                        JOptionPane.showMessageDialog(frame, e.toString(), tr("install_error"),
                                JOptionPane.ERROR_MESSAGE);
                        installButton.setEnabled(true);
                        return;
                    }
                    progress.setVisible(false);
                    JOptionPane.showMessageDialog(frame, tr("complete"), tr("complete_title"),
                            JOptionPane.INFORMATION_MESSAGE);
                    installButton.setEnabled(true);
                }).start();
            });

            frame.add(installButton);

            // show
            frame.setSize(WIDTH, HEIGHT);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Throwable e) {
            System.err.println("Couldn't open the installer window");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.toString(), tr("open_error"), JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private static String getGameFolder(JComboBox<String> box) {
        return Util.getDotMinecraft().resolve("axolotlclient-" + box.getSelectedItem().toString()).toAbsolutePath()
                .toString();
    }
}
