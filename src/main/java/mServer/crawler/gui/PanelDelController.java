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


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.mediathekview.mlib.daten.Sender;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class PanelDelController implements Initializable
{

    @FXML
    private GridPane pSender;
    @FXML
    private Label lblDeleted;
    @FXML
    private Label lblSender;

    private int i = 0;
    private List<Button> buttonSender;

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        initPanelDel();
    }

    private void initPanelDel()
    {
        lblDeleted.setText("");
        lblSender.setText("");
        buttonSender = new ArrayList<Button>();
        for (Sender sender : Sender.values())
        {
            final Button newButton = new Button(sender.getName());
            newButton.setOnAction(new PanelDelController.ActionDelSender(sender.getName()));
            buttonSender.add(newButton);
        }
        addSender();
    }

    private void addSender()
    {
        pSender.setHgap(10);
        pSender.setVgap(10);
        pSender.setPadding(new Insets(10));
        int zeile = 0, spalte = 0, count = 0;
        for (Sender aSender : Sender.values())
        {
            Button btn = buttonSender.get(count);
            btn.setText(aSender.getName());
            btn.setMaxWidth(Double.MAX_VALUE);
            pSender.add(btn, spalte, zeile);

            ++spalte;
            if (spalte >= 5)
            {
                ++zeile;
                spalte = 0;
            }
            ++count;
        }

    }

    private class ActionDelSender implements EventHandler<ActionEvent>
    {

        private final String sender;

        public ActionDelSender(String ssender)
        {
            sender = ssender;
        }

        @Override
        public void handle(ActionEvent t)
        {
            int before = Data.listeFilme.size();
            Data.listeFilme.deleteAllFilms(sender);
            int after = Data.listeFilme.size();
            lblDeleted.setText(before - after + "");
            lblSender.setText(sender);
            Data.mlibGuiController.lblSum.setText(Data.listeFilme.size() + "");
        }
    }

}
