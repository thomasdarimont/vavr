/**    / \____  _    ______   _____ / \____   ____  _____
 *    /  \__  \/ \  / \__  \ /  __//  \__  \ /    \/ __  \   Javaslang
 *  _/  // _\  \  \/  / _\  \\_  \/  // _\  \  /\  \__/  /   Copyright 2014 Daniel Dietrich
 * /___/ \_____/\____/\_____/____/\___\_____/_/  \_/____/    Licensed under the Apache License, Version 2.0
 */
package javaslang.collection;

import org.junit.Test;

import static javaslang.collection.BinaryTree.*;
import static org.assertj.core.api.Assertions.*;

public class BinaryTreeTest {

    // -- flatten

    /**
     *         1
     *        / \
     *       /   \
     *      /     \
     *     2       3
     *    / \     /
     *   4   5   6
     *  /       / \
     * 7       8   9
     *
     *
     */
    final BinaryTree<Integer> tree = branch(branch(branch(leaf(7), 4, nil()), 2, leaf(5)), 1, branch(branch(leaf(8), 6, leaf(9)), 3, nil()));

    @Test
    public void shouldFlattenTreeUsingPreOrder() {
        assertThat(tree.flatten(Traversal.PRE_ORDER)).isEqualTo(List.of(1, 2, 4, 7, 5, 3, 6, 8, 9));
    }

    @Test
    public void shouldFlattenTreeUsingInOrder() {
        assertThat(tree.flatten(Traversal.IN_ORDER)).isEqualTo(List.of(7, 4, 2, 5, 1, 8, 6, 9, 3));
    }

    @Test
    public void shouldFlattenTreeUsingPostOrder() {
        assertThat(tree.flatten(Traversal.POST_ORDER)).isEqualTo(List.of(7, 4, 5, 2, 8, 9, 6, 3, 1));
    }

    @Test
    public void shouldFlattenTreeUsingLevelOrder() {
        assertThat(tree.flatten(Traversal.LEVEL_ORDER)).isEqualTo(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    // -- map

    @Test
    public void shouldMapTree() {
        assertThat(tree.map(i -> (char) (i + 64)).toString()).isEqualTo("BinaryTree(A (B (D G) E) (C (F H I)))");
    }
}