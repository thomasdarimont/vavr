/*     / \____  _    _  ____   ______  / \ ____  __    _ _____
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  / /  _  \   Javaslang
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/  \__/  /   Copyright 2014-now Daniel Dietrich
 * /___/\_/  \_/\____/\_/  \_/\__\/__/___\_/  \_//  \__/_____/    Licensed under the Apache License, Version 2.0
 */
package javaslang.control;

import javaslang.AbstractValueTest;
import javaslang.control.Match.MatchValue;
import javaslang.control.Match.MatchValue.Then;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchValueTest extends AbstractValueTest {

    // -- AbstractValueTest

    @Override
    protected <R> Then<?, R> empty() {
        return Match.of(null).whenIs(1).then((R) null);
    }

    @Override
    protected <R> Then<?, R> of(R element) {
        return Match.of(null).whenIs(null).then(element);
    }

    @SafeVarargs
    @Override
    protected final <R> Then<?, R> of(R... elements) {
        return of(elements[0]);
    }

    @Override
    protected boolean useIsEqualToInsteadOfIsSameAs() {
        return true;
    }

    @Override
    protected int getPeekNonNilPerformingAnAction() {
        return 1;
    }

    // -- MatchValue

    // transform

    @Test
    public void shouldTransform() {
        final Function<Integer, String> transform = x -> Match.of(x)
                .whenIs(2).then(3)
                .otherwise(42)
                .transform(m -> String.valueOf(m.get()));
        assertThat(transform.apply(1)).isEqualTo("42");
        assertThat(transform.apply(2)).isEqualTo("3");
    }

    // when(Object)

    @Test
    public void shouldMatchNullByValue() {
        final boolean actual = Match.of(null).whenIs(null).then(true).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldMatchIntByValue() {
        final boolean actual = Match.of(2).whenIs(1).then(false).whenIs(2).then(true).get();
        assertThat(actual).isTrue();
    }

    // whenIn(Onject...)

    @Test
    public void shouldMatchNullByValueIn() {
        final boolean actual = Match.of(null).whenIsIn((Object) null).then(true).get();
        assertThat(actual).isTrue();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMatchNullVarargsByValueIn() {
        Match.of(null).whenIsIn((Object[]) null).then(false).get();
    }

    @Test
    public void shouldMatchIntByValueIn() {
        final boolean actual = Match.of(2).whenIsIn(1, 2, 3).then(true).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldMatchOrElseByValueIn() {
        final boolean actual = Match.of(4).whenIsIn(1, 2, 3).then(false).getOrElse(true);
        assertThat(actual).isTrue();
    }

    // whenTrue(Predicate)

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMatchNullVarargsByPredicate() {
        Match.of(null).when(null).then(false).get();
    }

    @Test
    public void shouldMatchEvenIntByPredicate() {
        final String divisibility = Match
                .of(0)
                .when((Number i) -> i.intValue() % 2 == 0)
                .then(() -> "even")
                .getOrElse("odd");
        assertThat(divisibility).isEqualTo("even");
    }

    @Test
    public void shouldMatchOddIntWithOrElseHavingUnmatchedPredicates() {
        final String divisibility = Match
                .of(1)
                .when((Integer i) -> i % 2 == 0)
                .then(() -> "even")
                .getOrElse("odd");
        assertThat(divisibility).isEqualTo("odd");
    }

    // whenType(Class)

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMatchNullByType() {
        Match.of(null).whenType(null).then(false).get();
    }

    @Test
    public void shouldMatchNumberByType() {
        final String actual = Match
                .of(1)
                .whenType(String.class)
                .then(s -> "String " + s)
                .whenType(Number.class)
                .then(n -> "Number " + n)
                .whenType(Integer.class)
                .then(i -> "int " + i)
                .getOrElse("unknown");
        assertThat(actual).isEqualTo("Number 1");
    }

    @Test
    public void shouldMatchOrElseByType() {
        final String actual = Match
                .of(1)
                .whenType(String.class)
                .then(s -> "String " + s)
                .whenType(Short.class)
                .then(s -> "Short " + s)
                .getOrElse("unknown");
        assertThat(actual).isEqualTo("unknown");
    }

    // whenTypeIn(Class...)

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMatchNullVarargsByTypeIn() {
        Match.of(null).whenTypeIn((Class<?>[]) null).then(false).get();
    }

    @Test
    public void shouldMatchNumberByTypeIn() {
        final Number number = 1;
        final String actual = Match
                .of(number)
                .whenTypeIn(Byte.class, Integer.class)
                .then(s -> "matched")
                .getOrElse("unknown");
        assertThat(actual).isEqualTo("matched");
    }

    @Test
    public void shouldMatchOrElseByTypeIn() {
        final Number number = 1;
        final String actual = Match
                .of(number)
                .whenTypeIn(Byte.class, Short.class)
                .then(s -> "matched")
                .getOrElse("unknown");
        assertThat(actual).isEqualTo("unknown");
    }

    // whenApplicable(Function1)

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMatchNullByFunction() {
        Match.of(null).whenApplicable(null).thenApply().get();
    }

    @Test
    public void shouldMatchIntByFunction() {
        final int actual = Match.of(1).whenApplicable((Integer i) -> i + 1).thenApply().get();
        assertThat(actual).isEqualTo(2);
    }

    @Test
    public void shouldMatchOrElseByFunction() {
        final boolean actual = Match.of(4).whenIsIn(1, 2, 3).then(false).getOrElse(true);
        assertThat(actual).isTrue();
    }

    // match by super-type

    @Test
    public void shouldMatchSuperTypeByValue() {
        final Option<Integer> option = Option.of(1);
        final boolean actual = Match.of(Option.some(1)).whenIs(option).then(true).get();
        assertThat(actual).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldMatchSuperTypeByValueIn() {
        final Option<Integer> option = Option.of(1);
        final boolean actual = Match.of(Option.some(1)).whenIsIn(Option.none(), option).then(true).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldMatchSuperTypeByPredicate() {
        final boolean actual = Match.of(Option.some(1)).when((Option<?> o) -> true).then(true).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldMatchSuperTypeByType() {
        final boolean actual = Match.of(Option.some(1)).whenType(Option.class).then(true).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldMatchSuperTypeByTypeIn() {
        final boolean actual = Match.of(Option.some(1)).whenTypeIn(Boolean.class, Option.class).then(true).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldMatchSuperTypeByFunction() {
        final boolean actual = Match.of(Option.some(1)).whenApplicable((Option<?> o) -> true).thenApply().get();
        assertThat(actual).isTrue();
    }

    // match by sub-type

    @Test
    public void shouldMatchSubTypeByValue() {
        final Option<Integer> option = Option.of(1);
        final boolean actual = Match.of(option).whenIs(Option.some(1)).then(true).get();
        assertThat(actual).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldMatchSubTypeByValueIn() {
        final Option<Integer> option = Option.of(1);
        final boolean actual = Match.of(option).whenIsIn(Option.none(), Option.some(1)).then(true).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldMatchSubTypeByPredicate() {
        final Option<Integer> option = Option.of(1);
        // when((Option.Some<?> o) -> true) is not possible any more
        final boolean actual = Match.of(option).when(o -> o instanceof Option.Some).then(true).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldMatchSubTypeByType() {
        final Option<Integer> option = Option.of(1);
        final boolean actual = Match.of(option).whenType(Option.Some.class).then(true).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldMatchSubTypeByTypeIn() {
        final Option<Integer> option = Option.of(1);
        final boolean actual = Match.of(option).whenTypeIn(Boolean.class, Option.Some.class).then(true).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldMatchSubTypeByFunction() {
        final Option<Integer> option = Option.of(1);
        final boolean actual = Match.of(option).whenApplicable((Option.Some<?> o) -> true).thenApply().get();
        assertThat(actual).isTrue();
    }

    // should apply then() when matched (honoring When, WhenApplicable)

    @Test
    public void shouldApplyThenValueWhenMatched() {
        final boolean actual = Match.of(1).whenIs(1).then(true).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldApplyThenSupplierWhenMatched() {
        final boolean actual = Match.of(1).whenIs(1).then(() -> true).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldApplyThenFunctionWhenMatched() {
        final boolean actual = Match.of(true).whenIs(true).then(b -> !b).get();
        assertThat(actual).isFalse();
    }

    @Test
    public void shouldApplyFunctionWhenApplicableMatched() {
        final boolean actual = Match.of(true).whenApplicable((Boolean b) -> !b).thenApply().get();
        assertThat(actual).isFalse();
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowWhenApplicableMatched() {
        Match.of(true).whenApplicable((Boolean b) -> !b).thenThrow(RuntimeException::new).get();
    }

    // should not apply then() when unmatched (honoring When, WhenApplicable)

    @Test
    public void shouldNotApplyThenValueWhenUnmatched() {
        final boolean actual = Match.of(0).whenIs(1).then(false).getOrElse(true);
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldNotApplyThenSupplierWhenUnmatched() {
        final boolean actual = Match.of(0).whenIs(1).then(() -> false).getOrElse(true);
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldNotApplyThenFunctionWhenUnmatched() {
        final boolean actual = Match.of(true).whenIs(false).then(b -> b).getOrElse(true);
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldNotApplyFunctionWhenApplicableUnmatched() {
        final boolean actual = Match.of(true).whenApplicable((Integer i) -> false).thenApply().getOrElse(true);
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldThrowWhenApplicableUnmatched() {
        final boolean actual = Match.of(true).whenApplicable((Integer b) -> false).thenThrow(RuntimeException::new).getOrElse(true);
        assertThat(actual).isTrue();
    }

    // otherwise() vs. getOrElse()

    @Test
    public void shouldUseOtherwiseToEnableMonadicOperationsWithMatched() {
        final double actual = Match.of(1).whenType(Number.class).then(n -> n).otherwise(() -> {
            throw new UnsupportedOperationException("not numeric");
        }).map(Number::doubleValue).get();
        assertThat(actual).isEqualTo(1.0d);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldUseOtherwiseToEnableMonadicOperationsWithUnmatched() {
        Match.of("1").whenType(Number.class).then(n -> n).otherwise(() -> {
            throw new UnsupportedOperationException("not numeric");
        }).map(Number::doubleValue).get();
    }

    @Test
    public void shouldUseOrElseToReturnAlternateValueWithMatched() {
        final double actual = Match.of(1).whenType(Number.class).then(Number::doubleValue).getOrElse(Double.NaN);
        assertThat(actual).isEqualTo(1.0d);
    }

    @Test
    public void shouldUseOrElseToReturnAlternateValueWithUnmatched() {
        final double actual = Match.of("1").whenType(Number.class).then(Number::doubleValue).getOrElse(Double.NaN);
        assertThat(actual).isNaN();
    }

    // transport matched states

    @Test
    public void shouldTransportMatchedValue() {
        final boolean actual = Match.of(1).whenIs(1).then(true).whenIs(1).then(false).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldTransportMatchedValueIn() {
        final boolean actual = Match.of(1).whenIsIn(1).then(true).whenIsIn(1).then(false).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldTransportMatchedPredicate() {
        final boolean actual = Match
                .of(1)
                .when((Integer i) -> true)
                .then(true)
                .when((Integer i) -> true)
                .then(false)
                .get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldTransportMatchedType() {
        final boolean actual = Match.of(1).whenType(Integer.class).then(true).whenType(Integer.class).then(false).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldTransportMatchedTypeIn() {
        final boolean actual = Match
                .of(1)
                .whenTypeIn(Integer.class)
                .then(true)
                .whenTypeIn(Integer.class)
                .then(false)
                .get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldTransportMatchedFunction() {
        final boolean actual = Match
                .of(1)
                .whenApplicable((Integer i) -> true)
                .thenApply()
                .whenApplicable((Integer i) -> false)
                .thenApply()
                .get();
        assertThat(actual).isTrue();
    }

    // transport unmatched states

    @Test
    public void shouldTransportUnmatchedValue() {
        final boolean actual = Match.of(1).whenIs(0).then(false).whenIs(1).then(true).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldTransportUnmatchedValueIn() {
        final boolean actual = Match.of(1).whenIsIn(0).then(false).whenIsIn(1).then(true).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldTransportUnmatchedPredicate() {
        final boolean actual = Match.of(1)
                .when((Integer i) -> i == -1).then(false)
                .when((Integer i) -> i == 1).then(true)
                .get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldTransportUnmatchedType() {
        final boolean actual = Match.of(1)
                .whenType(Boolean.class).then(false)
                .whenType(Integer.class).then(true)
                .get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldTransportUnmatchedTypeIn() {
        final boolean actual = Match.of(1)
                .whenTypeIn(Boolean.class).then(false)
                .whenTypeIn(Integer.class).then(true)
                .get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldTransportUnmatchedFunction() {
        final boolean actual = Match.of(1)
                .whenApplicable((Boolean i) -> false).thenApply()
                .whenApplicable((Integer i) -> true).thenApply()
                .get();
        assertThat(actual).isTrue();
    }

    // should declare common result type of multiple cases

    @Test
    public void shouldMatchTypedResultByValue() {
        final Number actual = Match.of(1).as(Number.class).whenIs(0).then(0.0d).whenIs(1).then(1).get();
        assertThat(actual).isEqualTo(1);
    }

    @Test(expected = MatchError.class)
    public void shouldMatchTypedResultByValueNegativeCase() {
        Match.of(-1).as(Number.class).whenIs(0).then(0.0d).whenIs(1).then(1).get();
    }

    @Test
    public void shouldMatchTypedResultByValueIn() {
        final Number actual = Match.of(1).as(Number.class).whenIsIn(0).then(0.0d).whenIsIn(1).then(1).get();
        assertThat(actual).isEqualTo(1);
    }

    @Test(expected = MatchError.class)
    public void shouldMatchTypedResultByValueInNegativeCase() {
        Match.of(-1).as(Number.class).whenIsIn(0).then(0.0d).whenIsIn(1).then(1).get();
    }

    @Test
    public void shouldMatchTypedResultByType() {
        final Number actual = Match
                .of(1)
                .as(Number.class)
                .whenType(Boolean.class)
                .then(0.0d)
                .whenType(Integer.class)
                .then(1)
                .get();
        assertThat(actual).isEqualTo(1);
    }

    @Test(expected = MatchError.class)
    public void shouldMatchTypedResultByTypeNegativeCase() {
        Match.of("x").as(Number.class).whenType(Boolean.class).then(0.0d).whenType(Integer.class).then(1).get();
    }

    @Test
    public void shouldMatchTypedResultByTypeIn() {
        final Number actual = Match
                .of(1)
                .as(Number.class)
                .whenTypeIn(Boolean.class)
                .then(0.0d)
                .whenTypeIn(Integer.class)
                .then(1)
                .get();
        assertThat(actual).isEqualTo(1);
    }

    @Test(expected = MatchError.class)
    public void shouldMatchTypedResultByTypeInNegativeCase() {
        Match.of("x").as(Number.class).whenTypeIn(Boolean.class).then(0.0d).whenTypeIn(Integer.class).then(1).get();
    }

    @Test
    public void shouldMatchTypedResultByFunction() {
        final Number actual = Match
                .of(1)
                .as(Number.class)
                .whenApplicable((Double d) -> d)
                .thenApply()
                .whenApplicable((Integer i) -> i)
                .thenApply()
                .get();
        assertThat(actual).isEqualTo(1);
    }

    @Test(expected = MatchError.class)
    public void shouldMatchTypedResultByFunctionNegativeCase() {
        Match
                .of("x")
                .as(Number.class)
                .whenApplicable((Double d) -> d)
                .thenApply()
                .whenApplicable((Integer i) -> i)
                .thenApply()
                .get();
    }

    // otherwise

    @Test
    public void shouldHandleOtherwiseValueOfMatched() {
        final boolean actual = Match.of(1).whenIs(1).then(true).otherwise(false).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldHandleOtherwiseValueOfUnmatched() {
        final boolean actual = Match.of(1).whenIs(0).then(false).otherwise(true).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldHandleOtherwiseSupplierOfMatched() {
        final boolean actual = Match.of(1).whenIs(1).then(true).otherwise(() -> false).get();
        assertThat(actual).isTrue();
    }

    @Test(expected = RuntimeException.class)
    public void shouldHandleOtherwiseThrowOfMatched() {
        Match.of(0).whenIs(1).then(true).otherwiseThrow(RuntimeException::new).get();
    }

    @Test
    public void shouldHandleOtherwiseThrowOfUnmatched() {
        final boolean actual = Match.of(1).whenIs(1).then(true).otherwiseThrow(RuntimeException::new).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldHandleOtherwiseSupplierOfUnmatched() {
        final boolean actual = Match.of(1).whenIs(0).then(false).otherwise(() -> true).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldHandleOtherwiseFunctionOfMatched() {
        final boolean actual = Match.of(1).whenIs(1).then(true).otherwise(i -> i != 1).get();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldHandleOtherwiseFunctionOfUnmatched() {
        final boolean actual = Match.of(1).whenIs(0).then(false).otherwise(i -> i == 1).get();
        assertThat(actual).isTrue();
    }

    // monadic operations

    @Test
    public void shouldFilterThen() {
        final Function<Boolean, Integer> f = b -> Match.of(1).whenIs(1).then(1).filter(i -> b).getOrElse(2);
        assertThat(f.apply(true)).isEqualTo(1);
        assertThat(f.apply(false)).isEqualTo(2);
    }

    @Test
    public void shouldSuccessFilterOtherwise() {
        final int actual = Match.of(0)
                .whenIs(1).then(1)
                .otherwise(2)
                .filter(i -> true)
                .get();
        assertThat(actual).isEqualTo(2);
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldFailFilterOtherwise() {
        Match.of(0).whenIs(1).then(1).otherwise(2).filter(i -> false).get();
    }

    @Test
    public void shouldMapMatched() {
        final int actual = Match.of(1).whenIs(1).then(1).map(i -> i + 1).get();
        assertThat(actual).isEqualTo(2);
    }

    @Test
    public void shouldMapUnmatched() {
        final int actual = Match.of(0).whenIs(1).then(1).map(i -> i + 1).getOrElse(-1);
        assertThat(actual).isEqualTo(-1);
    }

    @Test
    public void shouldMapOtherwise() {
        final int actual = Match.of(0)
                .whenIs(1).then(1)
                .otherwise(-1)
                .map(i -> i + 1)
                .get();
        assertThat(actual).isEqualTo(0);
    }

    // Traversable operations

    @Test
    public void shouldGetIteratorOfMatched() {
        final Iterator<Integer> actual = Match.of(1).whenIs(1).then(1).iterator();
        assertThat(actual.next()).isEqualTo(1);
        assertThat(actual.hasNext()).isFalse();
    }

    @Test
    public void shouldGetIteratorOfUnmatched() {
        final Iterator<Integer> actual = Match.of(0).whenIs(1).then(1).iterator();
        assertThat(actual.hasNext()).isFalse();
    }

    @Test
    public void shouldGetIteratorOfOtherwise() {
        final Iterator<Integer> actual = Match.of(0).whenIs(1).then(1).otherwise(-1).iterator();
        assertThat(actual.next()).isEqualTo(-1);
        assertThat(actual.hasNext()).isFalse();
    }

    // thenThrow

    @Test(expected = RuntimeException.class)
    public void shouldThrowEarly() {
        Match.of(null)
                .whenIs(null).thenThrow(RuntimeException::new);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowLateWhenMultipleCases() {
        Match.of(null)
                .whenIs(0).then(0)
                .whenIs(null).thenThrow(RuntimeException::new)
                .otherwise(0);
    }

    @Test
    public void shouldRunLateWhenMultipleCases() {
        int actual = Match.of(null)
                .whenIs(0).then(0)
                .whenIs(null).then(() -> 42)
                .get();
        assertThat(actual).isEqualTo(42);
    }

    @Test
    public void shouldConsumeWhenOnlyOtherwiseCase() {
        final IntegerConsumer integerConsumer = new IntegerConsumer();
        Match.of(1).otherwiseRun(integerConsumer);
        assertThat(integerConsumer.value).isEqualTo(1);
    }

    @Test
    public void shouldRunWhenOnlyOtherwiseCase() {
        final SimpleRunnable simpleRunnable = new SimpleRunnable();
        Match.of(1).otherwiseRun(simpleRunnable);
        assertThat(simpleRunnable.executed).isEqualTo(true);
    }

    @Test
    public void shouldConsumeMatchingCase() {
        final IntegerConsumer integerConsumer = new IntegerConsumer();
        Match.of(-1).whenIsIn(5, 3, -1).thenRun(integerConsumer);
        assertThat(integerConsumer.value).isEqualTo(-1);
    }

    @Test
    public void shouldRunMatchingCase() {
        final SimpleRunnable simpleRunnable = new SimpleRunnable();
        Match.of(-1).whenType(Number.class).thenRun(simpleRunnable);
        assertThat(simpleRunnable.executed).isEqualTo(true);
    }

    @Test
    public void shouldNotConsumeOtherwiseCaseWhenMatchingCaseFound() {
        final IntegerConsumer integerConsumer = new IntegerConsumer();
        Match.of(-1)
                .whenIsIn(5, 3, -1)
                .thenRun(integerConsumer)
                .otherwiseRun(i -> integerConsumer.accept(666));
        assertThat(integerConsumer.value).isEqualTo(-1);
    }

    @Test
    public void shouldNotRunOtherwiseCaseWhenMatchingCaseFound() {
        final SimpleRunnable simpleRunnable = new SimpleRunnable();
        Match.of(-1)
                .whenType(Number.class)
                .thenRun(simpleRunnable)
                .otherwiseRun(() -> {simpleRunnable.executed = false;});
        assertThat(simpleRunnable.executed).isEqualTo(true);
    }

    @Test
    public void shouldConsumeOtherwiseCaseWhenNoMatchingFound() {
        final IntegerConsumer integerConsumer = new IntegerConsumer();
        Match.of(-1)
                .whenIsIn(5, 3)
                .thenRun(integerConsumer)
                .otherwiseRun(i -> integerConsumer.accept(666));
        assertThat(integerConsumer.value).isEqualTo(666);
    }

    @Test
    public void shouldConsumeOnlyFirstMatchingCase() {
        final IntegerConsumer integerConsumer = new IntegerConsumer();
        Match.of(-1)
                .whenIs(3).thenRun(integerConsumer)
                .whenIs(4).thenRun(integerConsumer)
                .whenIsIn(5, 6).thenRun(integerConsumer)
                .whenType(Integer.class).thenRun(integerConsumer)
                .whenTypeIn(String.class, Boolean.class).thenRun(integerConsumer)
                .whenApplicable((Float f) -> {}).thenRun()
                .otherwiseRun(i -> integerConsumer.accept(666));
        assertThat(integerConsumer.value).isEqualTo(-1);
    }

    @Test
    public void shouldRunOnlyFirstMatchingCase() {
        final SimpleRunnable simpleRunnable = new SimpleRunnable();
        Match.of(-1)
                .whenIs(-1).thenRun(simpleRunnable)
                .whenIs(Integer.class).thenRun(() -> { simpleRunnable.executed = false; })
                .otherwiseRun(() -> { simpleRunnable.executed = false; });
        assertThat(simpleRunnable.executed).isEqualTo(true);
    }
}