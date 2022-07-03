package run.halo.app.extension.controller;

import reactor.core.Disposable;

public interface Controller extends Disposable {

    String getName();

    void start();

}
