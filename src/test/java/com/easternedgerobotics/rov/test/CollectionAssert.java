package com.easternedgerobotics.rov.test;

import org.junit.Assert;

import java.util.List;
import java.util.function.BiPredicate;

public final class CollectionAssert {
    private CollectionAssert() {
        // ???
    }

    public static <T> void assertItemsMatchPredicateInOrder(
        final List<T> a,
        final List<T> b,
        final BiPredicate<T, T> fn
    ) {
        if (a.size() != b.size()) {
            Assert.fail(String.format("expected lists of the same size: given <%d> and <%d>", a.size(), b.size()));
        }

        final int n = a.size();
        for (int i = 0; i < n; i++) {
            if (!fn.test(a.get(i), b.get(i))) {
                Assert.fail(String.format("%s and %s do not match", a.get(i), b.get(i)));
            }
        }
    }
}
