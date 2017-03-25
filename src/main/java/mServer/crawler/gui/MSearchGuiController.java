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

import de.mediathekview.mlib.filmlisten.FilmlisteLesen;
import de.mediathekview.mlib.filmlisten.WriteFilmlistJson;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

public class MSearchGuiController implements Initializable {

    @FXML
    private Parent fxPanelSearch;
    @FXML
    private PanelSearchController fxPanelSearchController;
    @FXML
    private Parent fxPanelDel;
    @FXML
    private PanelDelController fxPanelDelController;
    @FXML
    private Parent fxPanelTool;
    @FXML
    private PanelToolController fxPanelToolController;
    @FXML
    private TextField txtFilmList;
    @FXML
    public Label lblSum;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnSelect;
    @FXML
    private Button btnDelete;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Data.mlibGuiController = this;
//        System.out.println(fxPanelSearch);
//        System.out.println(fxPanelSearchController);
//        System.out.println(fxPanelDel);
//        System.out.println(fxPanelDelController);
//        System.out.println(fxPanelTool);
//        System.out.println(fxPanelToolController);

        initPanelGui();
    }

    private void initPanelGui() {
        if (Data.pathFilmlist.isEmpty()) {
            txtFilmList.setText(System.getProperty("user.home") + File.separator + ".mediathek3" + File.separator + "filme.json");
        } else {
            txtFilmList.setText(Data.pathFilmlist);
        }
        new FilmlisteLesen().readFilmListe(txtFilmList.getText(), Data.listeFilme, 0 /*all days*/);
        lblSum.setText(Data.listeFilme.size() + "");
        btnDelete.setOnAction(e -> {
            Data.listeFilme.clear();
            lblSum.setText(Data.listeFilme.size() + "");
        });
        btnSave.setOnAction(e -> new WriteFilmlistJson().filmlisteSchreibenJson(txtFilmList.getText(), Data.listeFilme));
        btnSelect.setOnAction(e -> getPath());
    }

    private void getPath() {
        FileChooser chooser = new FileChooser();
        if (!txtFilmList.getText().equals("")) {
            chooser.setInitialDirectory(
                    new File(System.getProperty("user.home"))
            );
        }
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            try {
                txtFilmList.setText(f.getAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
