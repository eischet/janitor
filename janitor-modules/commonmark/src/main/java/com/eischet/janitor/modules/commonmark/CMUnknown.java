// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.commonmark;

import org.commonmark.node.Node;

/** Wrapper for nodes whose type has no wrapper of its own yet. */
public class CMUnknown extends CMNode {

    public CMUnknown(final Node node) {
        super(node);
    }
}
