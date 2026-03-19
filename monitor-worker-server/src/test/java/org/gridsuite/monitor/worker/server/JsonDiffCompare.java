/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server;

import com.deblock.jsondiff.DiffGenerator;
import com.deblock.jsondiff.matcher.*;
import com.deblock.jsondiff.viewer.PatchDiffViewer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Radouane KHOUADRI {@literal <redouane.khouadri_externe at rte-france.com>}
 */
public class JsonDiffCompare {

    public static String compare(ObjectMapper OBJECT_MAPPER, Object w1, Object w2) throws JsonProcessingException {
        final var jsonMatcher = new CompositeJsonMatcher(
                //new IgnoredPathMatcher("id"),
                new NullEqualsEmptyArrayMatcher(),
                new LenientJsonArrayPartialMatcher(),
                new StrictJsonObjectPartialMatcher(),
                new StrictPrimitivePartialMatcher()
        );

        // Generate the diff
        final var diff1 = DiffGenerator.diff(OBJECT_MAPPER.writeValueAsString(w1), OBJECT_MAPPER.writeValueAsString(w2), jsonMatcher);
        final var patch = PatchDiffViewer.from(diff1);
        return patch.toString();
    }
}
