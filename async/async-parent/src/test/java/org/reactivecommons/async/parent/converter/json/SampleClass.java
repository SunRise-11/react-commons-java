package org.reactivecommons.async.parent.converter.json;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@RequiredArgsConstructor
@Getter
class SampleClass {
    private final String id;
    private final String name;
    private final Date date;
}
