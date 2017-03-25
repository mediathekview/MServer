/*
 * MediathekView
 * Copyright (C) 2016 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mServer.crawler.gui;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;

public class PanelToolController implements Initializable {

    @FXML
    private Button btnCheck;
    @FXML
    private Button btnPlay;
    @FXML
    private Button btnPath;
    @FXML
    private Button btnStop;
    @FXML
    private TextField txtPlay;
    @FXML
    private MediaView mv;
    private MediaPlayer mediaPlayer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initPanelTool();
    }

    private void initPanelTool() {
        btnCheck.setOnAction(e -> Data.listeFilme.check());
        btnPlay.setOnAction(e -> play());
        btnPath.setOnAction(e -> getPath());
        btnStop.setOnAction(e -> stop());
        txtPlay.setText("/tmp/film.mp4");
    }

    private void play() {
        File file = new File(txtPlay.getText());

        final String MEDIA_URL = file.toURI().toString();
        Media media = new Media(MEDIA_URL);
        mediaPlayer = new MediaPlayer(media);
        mv.setMediaPlayer(mediaPlayer);

        mediaPlayer.play();
    }

    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    private void getPath() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            try {
                txtPlay.setText(f.getAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
