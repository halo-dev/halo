package run.halo.app.extension.controller;

import reactor.core.Disposable;

public interface Synchronizer<R> extends Disposable {

    void start();

}
