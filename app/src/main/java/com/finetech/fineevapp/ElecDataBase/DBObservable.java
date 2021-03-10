package com.finetech.fineevapp.ElecDataBase;

public interface DBObservable {
    //register the observer with this method
    void registerDbObserver(com.finetech.fineevapp.ElecDataBase.DBObserver databaseObserver);
    //unregister the observer with this method
    void removeDbObserver(com.finetech.fineevapp.ElecDataBase.DBObserver databaseObserver);
}