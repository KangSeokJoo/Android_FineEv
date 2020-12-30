package com.jinasoft.fineevapp.ElecDataBase;

public interface DBObservable {
    //register the observer with this method
    void registerDbObserver(DBObserver databaseObserver);
    //unregister the observer with this method
    void removeDbObserver(DBObserver databaseObserver);
}