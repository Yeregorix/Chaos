/*
 * Copyright (c) 2019-2021 Hugo Dupanloup (Yeregorix)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.smoofyuniverse.chaos;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import net.smoofyuniverse.chaos.impl.TypeABuilder;
import net.smoofyuniverse.chaos.type.builder.TypeBuilder;
import net.smoofyuniverse.chaos.ui.UserInterface;
import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.environment.source.GitHubReleaseSource;

public class Chaos extends Application {
	private UserInterface ui;

	@Override
	public void init() {
		TypeBuilder.REGISTRY.put("A", TypeABuilder::new);
	}

	@Override
	public void run() {
		runLater(() -> {
			Stage stage = createStage(900, 700, "favicon.png");
			setStage(stage);

			Scene scene = new Scene(this.ui = new UserInterface());
			scene.setOnKeyPressed(e -> {
				if (e.getCode() == KeyCode.F11)
					stage.setFullScreen(true);
			});
			scene.setOnKeyTyped(e -> this.ui.keyTyped(e.getCharacter().charAt(0)));

			stage.setScene(scene);
			stage.show();
		});

		new Thread(this.ui::run).start();

		getManager().runUpdater(new GitHubReleaseSource("Yeregorix", "Chaos", null, "Chaos", getManager().getConnectionConfig()));
	}
}
