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
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

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
import javax.swing.WindowConstants;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.icons.FlatFileViewDirectoryIcon;

import io.github.axolotlclient.installer.util.DarkModeDetector;
import io.github.axolotlclient.installer.util.SimpleDocumentListener;
import io.github.axolotlclient.installer.util.Util;

/**
 * UI for installer.
 */
public final class InstallerApp {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 300;
    private static final int PROGRESS_WIDTH = 200;

    private final Installer installer = new Installer();

    private final JFrame frame = new JFrame(tr("title"));
    private final JLabel heading = new JLabel(tr("heading"));

    private final JLabel minecraftVersionLabel = new JLabel(tr("minecraft_version"));
    private final DefaultComboBoxModel<String> minecraftVersionModel = new DefaultComboBoxModel<>();
    private final JComboBox<String> minecraftVersionBox = new JComboBox<>(minecraftVersionModel);

    private final JLabel gameFolderLabel = new JLabel(tr("game_folder"));
    private final JTextField gameFolderBox = new JTextField();
    private final JButton gameFolderButton = new JButton(new FlatFileViewDirectoryIcon());
    private final JLabel gameFolderCreatedLabel = new JLabel(tr("game_folder_created"));
    private boolean gameFolderDirty;

    private final JButton installButton = new JButton(tr("install"));
    private final JProgressBar progress = new JProgressBar(0, 100);

    public static void main(String[] args) {
        if (DarkModeDetector.detect())
            FlatDarkLaf.setup();
        else
            FlatLightLaf.setup();

        new InstallerApp().run();
    }

    private void run() {
        try {
            init();

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

    private void init() throws IOException {
        setEnabled(false);

        // create a frame and use it for installation
        frame.setIconImage(ImageIO.read(Objects.requireNonNull(InstallerApp.class.getResourceAsStream("/icon.png"))));
        frame.setLayout(null);

        // add components
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 30));
        heading.setBounds(WIDTH / 2 - heading.getPreferredSize().width / 2, 14, heading.getPreferredSize().width,
                heading.getPreferredSize().height);
        frame.add(heading);

        minecraftVersionLabel.setBounds(WIDTH / 2 - minecraftVersionLabel.getPreferredSize().width / 2, 80,
                minecraftVersionLabel.getPreferredSize().width, minecraftVersionLabel.getPreferredSize().height);
        minecraftVersionBox.setBounds(WIDTH / 2 - 50,
                minecraftVersionLabel.getY() + minecraftVersionLabel.getHeight() + 3, 100,
                minecraftVersionBox.getPreferredSize().height);

        frame.add(minecraftVersionLabel);
        frame.add(minecraftVersionBox);

        gameFolderLabel.setBounds(WIDTH / 2 - gameFolderLabel.getPreferredSize().width / 2, 135,
                gameFolderLabel.getPreferredSize().width, gameFolderLabel.getPreferredSize().height);
        gameFolderBox.setBounds(WIDTH / 2 - 100, gameFolderLabel.getY() + gameFolderLabel.getHeight() + 3,
                200 - gameFolderBox.getPreferredSize().height - 5, gameFolderBox.getPreferredSize().height);
        gameFolderButton.setBounds(gameFolderBox.getX() + gameFolderBox.getWidth() + 5, gameFolderBox.getY(),
                gameFolderBox.getHeight(), gameFolderBox.getHeight());

        gameFolderCreatedLabel.setBounds(WIDTH / 2 - gameFolderCreatedLabel.getPreferredSize().width / 2,
                gameFolderBox.getY() + gameFolderBox.getPreferredSize().height + 3,
                gameFolderCreatedLabel.getPreferredSize().width, gameFolderCreatedLabel.getPreferredSize().height);
        gameFolderCreatedLabel.setVisible(false);

        gameFolderBox.getDocument().addDocumentListener((SimpleDocumentListener) event -> {
            gameFolderDirty = true;
            updateGameFolderCreated();
        });
        minecraftVersionBox.addItemListener(event -> {
            if (!gameFolderDirty) {
                gameFolderBox.setText(getGameFolder(minecraftVersionBox));
                gameFolderDirty = false;
            }
        });

        gameFolderButton.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser(new File(gameFolderBox.getText()));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setFileHidingEnabled(false);
            if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
                return;

            gameFolderBox.setText(chooser.getSelectedFile().getAbsolutePath());
            gameFolderDirty = true;
        });

        frame.add(gameFolderLabel);
        frame.add(gameFolderBox);
        frame.add(gameFolderButton);
        frame.add(gameFolderCreatedLabel);

        progress.setBounds(WIDTH / 2 - PROGRESS_WIDTH / 2, HEIGHT - 110, PROGRESS_WIDTH, 20);
        progress.setIndeterminate(true);
        progress.setStringPainted(true);
        progress.setString(tr("loading"));

        frame.add(progress);

        installButton.setBounds(WIDTH / 2 - installButton.getPreferredSize().width / 2, HEIGHT - 80,
                installButton.getPreferredSize().width, installButton.getPreferredSize().height);
        installButton.addActionListener(event -> install());

        frame.add(installButton);
        load();
    }

    private void load() {
        new Thread(() -> {
            try {
                installer.load();
            } catch (Throwable e) {
                setProgressVisible(false);
                System.err.println("Couldn't load");
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, e.toString(), tr("open_error"), JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            installer.getAvailableGameVers().forEach(item -> minecraftVersionModel.addElement(item));
            setProgressVisible(false);
            gameFolderDirty = false;
            setEnabled(true);
        }).start();
    }

    private void setEnabled(boolean enabled) {
        minecraftVersionBox.setEnabled(enabled);
        gameFolderBox.setEnabled(enabled);
        gameFolderButton.setEnabled(enabled);
        installButton.setEnabled(enabled);
    }

    private void install() {
        setEnabled(false);
        new Thread(() -> {
            try {
                Path gameDir = Paths.get(gameFolderBox.getText());

                Path modsDir = gameDir.resolve("mods");
                if (Files.isDirectory(modsDir) && (Files.list(modsDir).findAny().isPresent())) {
                    int opt = JOptionPane.showConfirmDialog(frame, tr("mods_present"), tr("mods_present_title"),
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    if (opt == JOptionPane.CANCEL_OPTION) {
                        setProgressVisible(false);
                        setEnabled(true);
                        return;
                    } else if (opt == JOptionPane.YES_OPTION) {
                        Files.walkFileTree(modsDir, new SimpleFileVisitor<Path>() {

                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                Files.delete(file);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory(Path file, IOException e) throws IOException {
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
                setProgressVisible(true);
                installer.install(installer.getModVerForGameVer(minecraftVersionBox.getSelectedItem().toString()),
                        Util.getDotMinecraft(), gameDir, ProgressConsumer.of(progress));
            } catch (Throwable e) {
                System.err.println("Couldn't install");
                e.printStackTrace();
                setProgressVisible(false);
                JOptionPane.showMessageDialog(frame, e.toString(), tr("install_error"), JOptionPane.ERROR_MESSAGE);
                setEnabled(true);
                return;
            }
            setProgressVisible(false);
            JOptionPane.showMessageDialog(frame, tr("complete"), tr("complete_title"), JOptionPane.INFORMATION_MESSAGE);
            setEnabled(true);
        }).start();
    }

    private void setProgressVisible(boolean visible) {
        progress.setVisible(visible);
        if (!visible)
            updateGameFolderCreated();
        else
            gameFolderCreatedLabel.setVisible(false);
    }

    private void updateGameFolderCreated() {
        gameFolderCreatedLabel.setVisible(!Files.exists(Paths.get(gameFolderBox.getText())));
    }

    private static String getGameFolder(JComboBox<String> box) {
        return Util.getDotMinecraft().resolve("axolotlclient-" + box.getSelectedItem().toString()).toAbsolutePath()
                .toString();
    }
}
