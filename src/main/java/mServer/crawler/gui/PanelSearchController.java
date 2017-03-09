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
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.filmesuchen.ListenerFilmeLaden;
import de.mediathekview.mlib.filmesuchen.ListenerFilmeLadenEvent;
import de.mediathekview.mlib.tool.Log;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import mServer.crawler.CrawlerConfig;
import mServer.crawler.GetUrl;

public class PanelSearchController implements Initializable {

    @FXML
    private Label lblSum;
    @FXML
    private Label lblProgress;
    @FXML
    private Label lblPercent;
    @FXML
    private Button btnStop;
    @FXML
    private Button btnAllSender;
    @FXML
    private Button btnLog;
    @FXML
    private ProgressBar pBar;
    @FXML
    private GridPane pSender;
    @FXML
    private RadioButton rbShort;
    @FXML
    private RadioButton rbLong;
    @FXML
    private RadioButton rbMax;
    @FXML
    private CheckBox cbLoadTime;
    @FXML
    private CheckBox cbDebug;
    @FXML
    private CheckBox cbUpdate;

    private int i = 0;
    private Button[] buttonSender;
    private String[] senderArray;
    private MSearchGuiLoad mlibGuiLoad;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnStop.setOnAction(e -> System.out.println("Test"));

        initPanelSearch();

    }

    private void initPanelSearch() {
        lblSum.setText("");
        btnStop.setOnAction(e -> {
            RotateTransition tr = new RotateTransition();
            tr.setNode(btnStop);
            tr.setDuration(Duration.millis(750));
            tr.setFromAngle(0);
            tr.setToAngle(220);
            tr.setAutoReverse(true);
            tr.setCycleCount(2);
            tr.play();

            Config.setStop(true);
        });
        mlibGuiLoad = new MSearchGuiLoad();

        rbShort.setSelected(true);
        CrawlerConfig.senderLoadHow = CrawlerConfig.LOAD_SHORT;
        rbShort.setOnAction(e -> CrawlerConfig.senderLoadHow = CrawlerConfig.LOAD_SHORT);
        rbLong.setOnAction(e -> CrawlerConfig.senderLoadHow = CrawlerConfig.LOAD_LONG);
        rbMax.setOnAction(e -> CrawlerConfig.senderLoadHow = CrawlerConfig.LOAD_MAX);

        GetUrl.showLoadTime = cbLoadTime.isSelected();
        cbLoadTime.setOnAction(e -> GetUrl.showLoadTime = cbLoadTime.isSelected());

        Config.debug = cbDebug.isSelected();
        cbDebug.setOnAction(e -> Config.debug = cbDebug.isSelected());

        cbUpdate.setOnAction(e -> CrawlerConfig.updateFilmliste = cbUpdate.isSelected());

        btnAllSender.setOnAction(e -> new Thread(() -> {
            disableButton(true);
            mlibGuiLoad.filmeBeimSenderSuchen(true);
        }).start());

        btnLog.setOnAction(e -> writeLog());

        senderArray = MSearchGuiLoad.getSenderNamen();
        buttonSender = new Button[senderArray.length];
        for (int i = 0; i < MSearchGuiLoad.getSenderNamen().length; ++i) {
            buttonSender[i] = new Button(senderArray[i]);
            buttonSender[i].setOnAction(new ActionLoadSender(senderArray[i]));
        }
        addSender();

        mlibGuiLoad.addAdListener(new ListenerFilmeLaden() {
            @Override
            public void progress(ListenerFilmeLadenEvent event) {
                Platform.runLater(() -> {
                    if (event.max == 0) {
                        pBar.setProgress(0);
                        lblPercent.setText("");
                    } else {
                        if (event.progress == 0) {
                            pBar.setProgress(0);
                            lblPercent.setText("0%");
                        } else {
                            double prog = 1.0 * event.progress / event.max;
                            if (prog < 0) {
                                prog = 0;
                            } else if (prog > 1) {
                                prog = 0.99;
                            }
                            pBar.setProgress(prog);
                            int i = (int) (100 * prog);
                            lblPercent.setText(i + "%");
                        }
                    }
                    lblProgress.setText(textLaenge(80, event.text, true /* mitte */, false /*addVorne*/));
                    lblSum.setText(event.count + "");
                });
            }

            @Override
            public void fertig(ListenerFilmeLadenEvent event) {
                Platform.runLater(() -> {
                    pBar.setProgress(0);
                    lblPercent.setText("");
                    lblSum.setText(Data.listeFilme.size() + "");
                    Data.mlibGuiController.lblSum.setText(Data.listeFilme.size() + "");

                    disableButton(false);
                });
            }
        });
    }

    private void writeLog() {
        final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String datei = "/tmp/testfile"; //////////////
        Date aktTime = new Date(System.currentTimeMillis());
        String aktTimeStr = sdf.format(aktTime);
        Log.sysLog("");
        Log.sysLog("Log schreiben: " + datei);
        Log.sysLog("--> " + aktTimeStr);
        File file = new File(datei);
        File dir = new File(file.getParent());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.errorLog(632012165, "Kann den Pfad nicht anlegen: " + dir.toString());
            }
        }

        try {
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8);
            out.write("===============================================================");
            out.write("===============================================================");
            out.write("\n");
            out.write("--> " + aktTimeStr);
            out.write("\n");
            ArrayList<String> ret;
            ret = mlibGuiLoad.msFilmeSuchen.endeMeldung();
            for (String s : ret) {
                out.write(s);
                out.write("\n");
            }
            ret = Log.printErrorMsg();
            for (String s : ret) {
                out.write(s);
                out.write("\n");
            }
            out.write("\n");
            out.write("\n");
            out.write("\n");
            out.write("\n");
            out.write("\n");
            out.write("\n");
            out.write("\n");
            out.write("\n");
            out.write("\n");
            out.write("\n");
            out.close();

            Log.sysLog("--> geschrieben!");
        } catch (Exception ex) {
            Log.errorLog(846930145, ex, "nach: " + datei);
        }
    }

    private void addSender() {
        pSender.setHgap(10);
        pSender.setVgap(10);
        pSender.setPadding(new Insets(10));
        int zeile = 0, spalte = 0, count = 0;
        for (String aSender : senderArray) {
            Button btn = buttonSender[count];
            btn.setText(aSender);
            btn.setMaxWidth(Double.MAX_VALUE);
            pSender.add(btn, spalte, zeile);

            ++spalte;
            if (spalte >= 5) {
                ++zeile;
                spalte = 0;
            }
            ++count;
        }

    }

    private static String textLaenge(int max, String text, boolean mitte, boolean addVorne) {
        if (text.length() > max) {
            if (mitte) {
                text = text.substring(0, 25) + " .... " + text.substring(text.length() - (max - 31));
            } else {
                text = text.substring(0, max - 1);
            }
        }
        while (text.length() < max) {
            if (addVorne) {
                text = " " + text;
            } else {
                text = text + " ";
            }
        }
        return text;
    }

    private void disableButton(boolean disable) {
        for (Button aButtonSender : buttonSender) {
            aButtonSender.setDisable(disable);
        }
        btnAllSender.setDisable(disable);
    }

    private class ActionLoadSender implements EventHandler<ActionEvent> {

        private final String sender;

        public ActionLoadSender(String ssender) {
            sender = ssender;
        }

        @Override
        public void handle(ActionEvent t) {
            lblProgress.setText("");
            lblSum.setText("");
            disableButton(true);
            mlibGuiLoad.updateSender(new String[]{sender} /* senderAllesLaden */);
        }
    }

}
