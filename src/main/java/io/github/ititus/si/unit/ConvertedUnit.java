package io.github.ititus.si.unit;

import io.github.ititus.si.prefix.Prefix;
import io.github.ititus.si.quantity.Quantity;
import io.github.ititus.si.quantity.type.QuantityType;
import io.github.ititus.si.unit.converter.MultiplicationConverter;
import io.github.ititus.si.unit.converter.UnitConverter;

import java.util.Objects;

final class ConvertedUnit<Q extends QuantityType<Q>> extends AbstractUnit<Q> {

    private final Unit<Q> baseUnit;
    private final UnitConverter converter;

    public static <Q extends QuantityType<Q>> Unit<Q> of(Unit<Q> baseUnit, UnitConverter converter) {
        if (converter.isIdentity()) {
            return baseUnit;
        }

        return new ConvertedUnit<>(baseUnit, converter);
    }

    private ConvertedUnit(Unit<Q> baseUnit, UnitConverter converter) {
        super(baseUnit.getType(), baseUnit.getDimension());
        this.baseUnit = baseUnit;
        this.converter = converter;
    }

    @Override
    public String getSymbol() {
        throw new UnsupportedOperationException("converted units have not symbol");
    }

    @Override
    public <T extends QuantityType<T>> UnitConverter getConverterTo(Unit<T> unit) {
        if (!isCommensurableWith(unit.getType())) {
            throw new ClassCastException();
        } else if (equals(unit)) {
            return UnitConverter.IDENTITY;
        } else if (baseUnit.equals(unit)) {
            return converter;
        }

        return converter.concat(baseUnit.getConverterTo(unit));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends QuantityType<T>> Unit<T> as(T type) {
        if (!isCommensurableWith(type)) {
            throw new ClassCastException();
        } else if (getType().equals(type)) {
            return (Unit<T>) this;
        }

        return new ConvertedUnit<>(baseUnit.as(type), converter);
    }

    @Override
    public Unit<Q> multiply(double d) {
        return of(baseUnit, converter.concat(MultiplicationConverter.of(d)));
    }

    @Override
    public Unit<?> multiply(Unit<?> unit) {
        return CompoundUnit.ofProduct(this, unit);
    }

    @Override
    public Unit<?> inverse() {
        return CompoundUnit.inverse(this);
    }

    @Override
    public Unit<?> pow(int n) {
        return CompoundUnit.ofPow(this, n);
    }

    @Override
    public Unit<?> root(int n) {
        return CompoundUnit.ofRoot(this, n);
    }

    @Override
    public Unit<Q> alternate(String symbol) {
        return new AlternateUnit<>(this, symbol);
    }

    @Override
    public Unit<Q> prefix(Prefix prefix) {
        return new PrefixUnit<>(this, prefix);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConvertedUnit)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ConvertedUnit<?> that = (ConvertedUnit<?>) o;
        return baseUnit.equals(that.baseUnit) && converter.equals(that.converter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), baseUnit, converter);
    }
}
