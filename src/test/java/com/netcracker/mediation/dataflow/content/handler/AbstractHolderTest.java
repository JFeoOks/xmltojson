package com.netcracker.mediation.dataflow.content.handler;

import org.xml.sax.Attributes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractHolderTest {

    public Attributes getMockedAttributes() {
        Attributes mock = mock(Attributes.class);
        when(mock.getLength()).thenReturn(3);
        when(mock.getQName(0)).thenReturn("int");
        when(mock.getQName(1)).thenReturn("bool");
        when(mock.getQName(2)).thenReturn("str");

        when(mock.getValue(0)).thenReturn("1");
        when(mock.getValue(1)).thenReturn("true");
        when(mock.getValue(2)).thenReturn("hello");

        return mock;
    }
}
