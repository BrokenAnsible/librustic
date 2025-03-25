package io.naiant.librustic;

record Error<E extends Exception, T>(E exception) implements Result<T> {}
