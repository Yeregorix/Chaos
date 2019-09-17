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

package net.smoofyuniverse.chaos.impl;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import net.smoofyuniverse.chaos.type.builder.ColoredTypeBuilder;
import net.smoofyuniverse.chaos.universe.Universe;
import net.smoofyuniverse.common.fx.field.DoubleField;
import net.smoofyuniverse.common.util.GridUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Optional;

public class TypeABuilder implements ColoredTypeBuilder<TypeA> {
	public static final int CURRENT_VERSION = 1, MINIMUM_VERSION = 1;

	public final ObjectProperty<Color> color = new SimpleObjectProperty<>();
	public final DoubleField radius = new DoubleField(0, 100, 5), friction = new DoubleField(0, 1, 0.1),
			attractionFactor = new DoubleField(-50, 50, 0.1), attractionRadius = new DoubleField(0, 500, 10),
			repulsionFactor = new DoubleField(-50, 50, 1), repulsionRadius = new DoubleField(0, 500, 7),
			receptionAngleDeg = new DoubleField(-360, 360, 0), emissionAngleDeg = new DoubleField(-360, 360, 0);
	public final CheckBox flatAttraction = new CheckBox();

	private GridPane pane = new GridPane();

	public TypeABuilder(Color color, double radius, double friction, double attractionFactor, double attractionRadius, double repulsionFactor, double repulsionRadius, double receptionAngleDeg, double emissionAngleDeg, boolean flatAttraction) {
		this();

		this.color.set(color);
		this.radius.setValue(radius);
		this.friction.setValue(friction);
		this.attractionFactor.setValue(attractionFactor);
		this.attractionRadius.setValue(attractionRadius);
		this.repulsionFactor.setValue(repulsionFactor);
		this.repulsionRadius.setValue(repulsionRadius);
		this.receptionAngleDeg.setValue(receptionAngleDeg);
		this.emissionAngleDeg.setValue(emissionAngleDeg);
		this.flatAttraction.setSelected(flatAttraction);
	}

	public TypeABuilder() {
		this.radius.setMaxWidth(Double.MAX_VALUE);
		this.friction.setMaxWidth(Double.MAX_VALUE);
		this.attractionFactor.setMaxWidth(Double.MAX_VALUE);
		this.attractionRadius.setMaxWidth(Double.MAX_VALUE);
		this.repulsionFactor.setMaxWidth(Double.MAX_VALUE);
		this.repulsionRadius.setMaxWidth(Double.MAX_VALUE);
		this.receptionAngleDeg.setMaxWidth(Double.MAX_VALUE);
		this.emissionAngleDeg.setMaxWidth(Double.MAX_VALUE);

		this.pane.add(new Label("Radius:"), 0, 0);
		this.pane.add(this.radius, 1, 0);
		this.pane.add(new Label("Friction:"), 2, 0);
		this.pane.add(this.friction, 3, 0);

		this.pane.add(new Label("Attraction factor:"), 0, 1);
		this.pane.add(this.attractionFactor, 1, 1);
		this.pane.add(new Label("Attraction radius:"), 2, 1);
		this.pane.add(this.attractionRadius, 3, 1);

		this.pane.add(new Label("Repulsion factor:"), 0, 2);
		this.pane.add(this.repulsionFactor, 1, 2);
		this.pane.add(new Label("Repulsion radius:"), 2, 2);
		this.pane.add(this.repulsionRadius, 3, 2);

		this.pane.add(new Label("Reception angle:"), 0, 3);
		this.pane.add(this.receptionAngleDeg, 1, 3);
		this.pane.add(new Label("Emission angle:"), 2, 3);
		this.pane.add(this.emissionAngleDeg, 3, 3);

		this.pane.add(new Label("Flat attraction:"), 0, 4);
		this.pane.add(this.flatAttraction, 1, 4);

		this.pane.setHgap(5);
		this.pane.setVgap(5);

		this.pane.getColumnConstraints().addAll(GridUtil.createColumn(15), GridUtil.createColumn(35), GridUtil.createColumn(15), GridUtil.createColumn(35));
	}

	@Override
	public String getTypeName() {
		return "A";
	}

	@Override
	public Optional<TypeA> build(Universe universe) {
		try {
			return Optional.of(new TypeA(universe, this.color.get(),
					this.radius.getValue(), this.friction.getValue(),
					this.attractionFactor.getValue(), this.attractionRadius.getValue(),
					this.repulsionFactor.getValue(), this.repulsionRadius.getValue(),
					this.receptionAngleDeg.getValue() / 180.0 * Math.PI, this.emissionAngleDeg.getValue() / 180.0 * Math.PI,
					this.flatAttraction.isSelected()));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public Node getNode() {
		return this.pane;
	}

	@Override
	public void read(DataInputStream in) throws IOException {
		int version = in.readInt();
		if (version > CURRENT_VERSION || version < MINIMUM_VERSION)
			throw new IOException("Invalid format version: " + version);

		this.color.set(Color.color(in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble()));

		this.radius.setValue(in.readDouble());
		this.friction.setValue(in.readDouble());
		this.attractionFactor.setValue(in.readDouble());
		this.attractionRadius.setValue(in.readDouble());
		this.repulsionFactor.setValue(in.readDouble());
		this.repulsionRadius.setValue(in.readDouble());
		this.receptionAngleDeg.setValue(in.readDouble());
		this.emissionAngleDeg.setValue(in.readDouble());
		this.flatAttraction.setSelected(in.readBoolean());
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(CURRENT_VERSION);

		Color c = this.color.get();
		out.writeDouble(c.getRed());
		out.writeDouble(c.getGreen());
		out.writeDouble(c.getBlue());
		out.writeDouble(c.getOpacity());

		out.writeDouble(this.radius.getValue());
		out.writeDouble(this.friction.getValue());
		out.writeDouble(this.attractionFactor.getValue());
		out.writeDouble(this.attractionRadius.getValue());
		out.writeDouble(this.repulsionFactor.getValue());
		out.writeDouble(this.repulsionRadius.getValue());
		out.writeDouble(this.receptionAngleDeg.getValue());
		out.writeDouble(this.emissionAngleDeg.getValue());
		out.writeBoolean(this.flatAttraction.isSelected());
	}

	@Override
	public ObjectProperty<Color> colorProperty() {
		return this.color;
	}
}
