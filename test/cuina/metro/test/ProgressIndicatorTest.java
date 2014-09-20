/*
 * JavaFX 8 Indetermine skin for ProgressIndicator control,
 * inspired by Windows 8's ProgressRing.
 * Copyright (c) 2014, fireandfuel (fireandfuel<at>hotmail<dot>de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * See the file COPYING included with this distribution for more
 * information.
 */

package cuina.metro.test;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProgressIndicatorTest extends Application
{

	@Override
	public void start(Stage stage) throws Exception
	{
		stage.setTitle("ProgressIndicatorTest");
		stage.setResizable(false);

		BorderPane pane = new BorderPane();
		VBox box = new VBox(15.0d);
		box.setAlignment(Pos.CENTER);
		ProgressIndicator indicator = new ProgressIndicator(
				ProgressIndicator.INDETERMINATE_PROGRESS);
		box.getChildren().add(indicator);
		box.getChildren().add(new Label("ProgressIndicatorTest"));
		pane.setCenter(box);

		Scene scene = new Scene(pane, 200, 200);
		scene.getStylesheets().add(
				this.getClass().getResource("/cuina/metro/css/theme.css").toExternalForm());
		stage.setScene(scene);

		stage.show();
	}

	public static void main(String[] args)
	{
		Application.launch(args);
	}

}
