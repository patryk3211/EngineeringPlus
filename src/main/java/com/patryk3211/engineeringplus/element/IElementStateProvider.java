package com.patryk3211.engineeringplus.element;

public interface IElementStateProvider {
    Element.State getState(float temperature, float pressure);
}
