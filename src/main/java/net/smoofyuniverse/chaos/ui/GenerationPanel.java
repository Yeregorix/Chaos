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

package net.smoofyuniverse.chaos.ui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.StringConverter;
import net.smoofyuniverse.chaos.impl.gen.TypeAGenerator;
import net.smoofyuniverse.chaos.impl.gen.TypeAGenerators;
import net.smoofyuniverse.chaos.type.Type;
import net.smoofyuniverse.chaos.type.builder.ColoredTypeBuilder;
import net.smoofyuniverse.chaos.type.builder.TypeBuilder;
import net.smoofyuniverse.chaos.type.gen.TypeGenerator;
import net.smoofyuniverse.chaos.universe.Universe;
import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.fx.field.IntegerField;
import net.smoofyuniverse.common.util.GridUtil;
import net.smoofyuniverse.logger.core.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class GenerationPanel extends GridPane {
	public static final int CURRENT_VERSION = 1, MINIMUM_VERSION = 1;
	private static final Logger logger = Logger.get("GenerationPanel");

	private final ListView<TypeObject> types = new ListView<>();
	private final TextField seed1 = new TextField(), seed2 = new TextField();

	private final Random optionsRandom = new Random();
	private long genSeed;
	private int ct;

	public GenerationPanel() {
		Button assignColors = new Button("Assign colors"), clear = new Button("Clear");
		ChoiceBox<TypeGenerator<?>> generator = new ChoiceBox<>();
		Button addOne = new Button("Add one"), addMany = new Button("Add many");
		Button rSeed1 = new Button("Random"), rSeed2 = new Button("Random");
		Button open = new Button("Open"), save = new Button("Save");

		assignColors.setMaxWidth(Double.MAX_VALUE);
		clear.setMaxWidth(Double.MAX_VALUE);
		generator.setMaxWidth(Double.MAX_VALUE);
		addOne.setMaxWidth(Double.MAX_VALUE);
		addMany.setMaxWidth(Double.MAX_VALUE);
		this.seed1.setMaxWidth(Double.MAX_VALUE);
		this.seed2.setMaxWidth(Double.MAX_VALUE);
		rSeed1.setMaxWidth(Double.MAX_VALUE);
		rSeed2.setMaxWidth(Double.MAX_VALUE);
		open.setMaxWidth(Double.MAX_VALUE);
		save.setMaxWidth(Double.MAX_VALUE);

		this.types.setCellFactory(c -> new TypeCell());
		assignColors.setOnAction(e -> assignColors());
		clear.setOnAction(e -> this.types.getItems().clear());

		generator.setConverter(new StringConverter<TypeGenerator<?>>() {
			@Override
			public String toString(TypeGenerator<?> obj) {
				return obj.getDisplayName();
			}

			@Override
			public TypeGenerator<?> fromString(String string) {
				throw new UnsupportedOperationException();
			}
		});

		generator.getItems().addAll(TypeAGenerators.DEFAULT, TypeAGenerators.WANDERER);
		generator.getItems().addAll(TypeAGenerators.RANDOMS);

		addOne.setOnAction(e -> {
			TypeGenerator<?> gen = generator.getSelectionModel().getSelectedItem();
			if (gen == null)
				return;

			TypeBuilder<?> b = gen.generate(this.optionsRandom);
			if (b instanceof ColoredTypeBuilder)
				((ColoredTypeBuilder) b).colorProperty().set(Color.hsb(this.optionsRandom.nextDouble() * 360, 1d, this.ct++ % 2 == 0 ? 1d : 0.6d));
			this.types.getItems().add(new TypeObject(b));
		});

		addMany.setOnAction(e -> {
			TypeGenerator<?> gen = generator.getSelectionModel().getSelectedItem();
			if (gen == null)
				return;

			int c = gen.recommendedTypes();
			int d = (160 / c) * 10;
			for (int i = 0; i < c; i++) {
				TypeBuilder<?> b = gen.generate(this.optionsRandom);
				if (b instanceof ColoredTypeBuilder)
					((ColoredTypeBuilder) b).colorProperty().set(Color.hsb(this.optionsRandom.nextDouble() * 360, 1d, this.ct++ % 2 == 0 ? 1d : 0.6d));
				TypeObject obj = new TypeObject(b);
				obj.count.setValue(d);
				this.types.getItems().add(obj);
			}
		});

		this.seed1.textProperty().addListener((v, oldV, newV) -> {
			long l;
			try {
				l = Long.parseLong(newV);
			} catch (NumberFormatException e) {
				l = newV.hashCode();
			}
			this.genSeed = l;
		});

		this.seed2.textProperty().addListener((v, oldV, newV) -> {
			long l;
			try {
				l = Long.parseLong(newV);
			} catch (NumberFormatException e) {
				l = newV.hashCode();
			}
			this.optionsRandom.setSeed(l);
		});

		rSeed1.setOnAction(e -> this.seed1.setText(randomSeed()));
		rSeed2.setOnAction(e -> this.seed2.setText(randomSeed()));

		this.seed1.setText(randomSeed());
		this.seed2.setText(randomSeed());

		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter("Chaos Options", "*.cho"));

		// TODO async IO
		open.setOnAction(e -> {
			File f = chooser.showOpenDialog(Application.get().getStage().orElse(null));
			if (f != null) {
				Path p = f.toPath();
				try {
					read(p);
				} catch (IOException ex) {
					logger.error("Failed to read options from " + p.getFileName(), ex);
				}
			}
		});

		save.setOnAction(e -> {
			File f = chooser.showSaveDialog(Application.get().getStage().orElse(null));
			if (f != null) {
				Path p = f.toPath();
				try {
					write(p);
				} catch (IOException ex) {
					logger.error("Failed to write options to " + p.getFileName(), ex);
				}
			}
		});

		add(this.types, 0, 0, 5, 1);

		addRow(1, assignColors, clear, generator, addOne, addMany);

		add(new Label("Generation seed:"), 0, 2);
		add(this.seed1, 1, 2, 3, 1);
		add(rSeed1, 4, 2);

		add(new Label("Options seed:"), 0, 3);
		add(this.seed2, 1, 3, 3, 1);
		add(rSeed2, 4, 3);

		add(open, 0, 4);
		add(save, 1, 4);

		setPadding(new Insets(5));
		setVgap(5);
		setHgap(5);

		getColumnConstraints().addAll(GridUtil.createColumn(17), GridUtil.createColumn(17), GridUtil.createColumn(32), GridUtil.createColumn(17), GridUtil.createColumn(17));
		getRowConstraints().addAll(GridUtil.createRow(Priority.ALWAYS), GridUtil.createRow(), GridUtil.createRow(), GridUtil.createRow(), GridUtil.createRow());

		// Default config
		int index = this.optionsRandom.nextInt(TypeAGenerators.RANDOMS.size());
		generator.getSelectionModel().select(2 + index);

		TypeAGenerator gen = TypeAGenerators.RANDOMS.get(index);
		int c = gen.recommendedTypes();
		int d = (160 / c) * 10;
		for (int i = 0; i < c; i++) {
			TypeObject obj = new TypeObject(gen.generate(this.optionsRandom));
			obj.count.setValue(d);
			this.types.getItems().add(obj);
		}
		assignColors();

		this.types.getItems().add(new TypeObject(TypeAGenerators.WANDERER.generate(this.optionsRandom)));
	}

	private void assignColors() {
		ObservableList<TypeObject> l = this.types.getItems();

		int count = 0;
		for (TypeObject obj : l) {
			if (obj.builder instanceof ColoredTypeBuilder)
				count++;
		}

		double offset = this.optionsRandom.nextDouble();

		int i = 0;
		for (TypeObject obj : l) {
			if (obj.builder instanceof ColoredTypeBuilder)
				((ColoredTypeBuilder) obj.builder).colorProperty().set(Color.hsb(((i++ / (double) count) + offset) * 360, 1d, i % 2 == 0 ? 1d : 0.6d));
		}
	}

	private static String randomSeed() {
		return Long.toString(ThreadLocalRandom.current().nextLong());
	}

	public void read(Path file) throws IOException {
		try (DataInputStream in = new DataInputStream(Files.newInputStream(file))) {
			read(in);
		}
	}

	public void write(Path file) throws IOException {
		try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(file))) {
			write(out);
		}
	}

	public void read(DataInputStream in) throws IOException {
		int version = in.readInt();
		if (version > CURRENT_VERSION || version < MINIMUM_VERSION)
			throw new IOException("Invalid format version: " + version);

		String s1 = in.readUTF(), s2 = in.readUTF();

		int size = in.readInt();
		List<TypeObject> l = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			String typeName = in.readUTF();
			Supplier<TypeBuilder<?>> supplier = TypeBuilder.REGISTRY.get(typeName);
			if (supplier == null) {
				logger.warn("Skipping unknown type name " + typeName);
				continue;
			}

			TypeObject obj = new TypeObject(supplier.get());
			obj.builder.read(in);
			obj.count.set(in.readInt());

			l.add(obj);
		}

		this.seed1.setText(s1);
		this.seed2.setText(s2);
		this.types.getItems().setAll(l);
	}

	public void write(DataOutputStream out) throws IOException {
		out.writeInt(CURRENT_VERSION);

		out.writeUTF(this.seed1.getText());
		out.writeUTF(this.seed2.getText());

		List<TypeObject> l = this.types.getItems();
		out.writeInt(l.size());
		for (TypeObject t : l) {
			out.writeUTF(t.builder.getTypeName());
			t.builder.write(out);
			out.writeInt(t.count.get());
		}
	}

	public void generateParticles(Universe universe) {
		Random r = new Random(this.genSeed);
		for (TypeObject cfg : this.types.getItems()) {
			Type type = cfg.builder.build(universe).orElse(null);
			if (type != null) {
				int c = cfg.count.getValue();
				for (int i = 0; i < c; i++)
					universe.add(type.createRandom(r));
			}
		}
	}

	public static class TypeObject {
		public final IntegerProperty count = new SimpleIntegerProperty(1);
		public final TypeBuilder<?> builder;

		public TypeObject(TypeBuilder<?> builder) {
			this.builder = builder;
		}
	}

	public class TypeCell extends ListCell<TypeObject> {
		private final Label typeName = new Label();
		private final IntegerField count = new IntegerField(0, 3000);
		private final ColorPicker colorPicker = new ColorPicker();
		private final StackPane options = new StackPane();
		private final GridPane pane = new GridPane();
		private IntegerProperty curCount;
		private ObjectProperty<Color> curColor;

		public TypeCell() {
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

			this.typeName.setStyle("-fx-font-weight: bold;");

			Button remove = new Button("Remove");
			remove.setOnAction(e -> GenerationPanel.this.types.getItems().remove(getIndex()));

			this.count.setMaxWidth(Double.MAX_VALUE);
			this.colorPicker.setMaxWidth(Double.MAX_VALUE);
			remove.setMaxWidth(Double.MAX_VALUE);

			this.pane.addRow(0, this.typeName, this.count, this.colorPicker, remove);
			this.pane.add(this.options, 0, 1, 4, 1);

			this.pane.setVgap(5);
			this.pane.setHgap(5);

			this.pane.getColumnConstraints().addAll(GridUtil.createColumn(15), GridUtil.createColumn(35), GridUtil.createColumn(35), GridUtil.createColumn(15));
			this.pane.getRowConstraints().addAll(GridUtil.createRow(), GridUtil.createRow(Priority.ALWAYS));
		}

		@Override
		protected void updateItem(TypeObject item, boolean empty) {
			super.updateItem(item, empty);

			// Unbind previous values
			if (this.curCount != null)
				this.count.valueProperty().unbindBidirectional(this.curCount);
			if (this.curColor != null)
				this.colorPicker.valueProperty().unbindBidirectional(this.curColor);

			if (empty) {
				setGraphic(null);
			} else {
				// Update content
				this.typeName.setText("Type: " + item.builder.getTypeName());

				this.curCount = item.count;
				this.count.valueProperty().bindBidirectional(this.curCount);

				if (item.builder instanceof ColoredTypeBuilder) {
					this.curColor = ((ColoredTypeBuilder) item.builder).colorProperty();
					this.colorPicker.valueProperty().bindBidirectional(this.curColor);
					this.colorPicker.setDisable(false);
				} else {
					this.colorPicker.setDisable(true);
				}

				this.options.getChildren().setAll(item.builder.getNode());

				setGraphic(this.pane);
			}
		}
	}
}
