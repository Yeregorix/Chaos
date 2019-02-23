/*
 * Copyright (c) 2019 Hugo Dupanloup (Yeregorix)
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
import net.smoofyuniverse.chaos.ui.UserInterface;
import net.smoofyuniverse.common.app.App;
import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.app.Arguments;

import java.util.concurrent.Executors;

public class Chaos extends Application {
	private UserInterface ui;

	public Chaos(Arguments args) {
		super(args, "Chaos", "1.0.3");
	}

	@Override
	public void init() {
		requireUI();
		initServices(Executors.newCachedThreadPool());

		App.runLater(() -> {
			initStage(900, 700, true, "favicon.png");

			Scene scene = new Scene(this.ui = new UserInterface());
			scene.setOnKeyPressed(e -> {
				if (e.getCode() == KeyCode.F11)
					getStage().get().setFullScreen(true);
				else
					this.ui.keyPressed(e.getCode());
			});
			setScene(scene).show();
		});

		checkForUpdate();

		new Thread(this.ui::run).start();
	}

	public static void main(String[] args) {
		new Chaos(Arguments.parse(args)).launch();
	}
}
