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
import net.smoofyuniverse.common.fxui.field.DoubleField;
import net.smoofyuniverse.common.util.GridUtil;

import java.util.Optional;

public class TypeABuilder implements ColoredTypeBuilder<TypeA> {
	public final ObjectProperty<Color> color = new SimpleObjectProperty<>();
	public final DoubleField radius = new DoubleField(0, 100, 5), friction = new DoubleField(0, 1, 0.1),
			attractionFactor = new DoubleField(0, 50, 0.1), attractionRadius = new DoubleField(0, 500, 10),
			repulsionFactor = new DoubleField(0, 50, 1), repulsionRadius = new DoubleField(0, 500, 7);
	public final CheckBox flatAttraction = new CheckBox();

	private GridPane pane = new GridPane();

	public TypeABuilder(TypeA value) {
		this();
		set(value);
	}

	public TypeABuilder() {
		this.radius.setMaxWidth(Double.MAX_VALUE);
		this.friction.setMaxWidth(Double.MAX_VALUE);
		this.attractionFactor.setMaxWidth(Double.MAX_VALUE);
		this.attractionRadius.setMaxWidth(Double.MAX_VALUE);
		this.repulsionFactor.setMaxWidth(Double.MAX_VALUE);
		this.repulsionRadius.setMaxWidth(Double.MAX_VALUE);

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

		this.pane.add(new Label("Flat attraction:"), 0, 3);
		this.pane.add(this.flatAttraction, 1, 3);

		this.pane.setHgap(3);
		this.pane.setVgap(3);

		this.pane.getColumnConstraints().addAll(GridUtil.createColumn(15), GridUtil.createColumn(35), GridUtil.createColumn(15), GridUtil.createColumn(35));
	}

	@Override
	public void set(TypeA value) {
		this.color.set(value.color);
		this.radius.setValue(value.radius);
		this.friction.setValue(value.friction);
		this.attractionFactor.setValue(value.attractionFactor);
		this.attractionRadius.setValue(value.attractionRadius);
		this.repulsionFactor.setValue(value.repulsionFactor);
		this.repulsionRadius.setValue(value.repulsionRadius);
		this.flatAttraction.setSelected(value.flatAttraction);
	}

	@Override
	public Optional<TypeA> build() {
		try {
			return Optional.of(new TypeA(this.color.get(),
					this.radius.getValue(), this.friction.getValue(),
					this.attractionFactor.getValue(), this.attractionRadius.getValue(),
					this.repulsionFactor.getValue(), this.repulsionRadius.getValue(),
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
	public ObjectProperty<Color> colorProperty() {
		return this.color;
	}
}
