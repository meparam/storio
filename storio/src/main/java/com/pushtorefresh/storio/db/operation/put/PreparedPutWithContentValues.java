package com.pushtorefresh.storio.db.operation.put;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.db.StorIODb;
import com.pushtorefresh.storio.db.operation.Changes;

import rx.Observable;
import rx.Subscriber;

public class PreparedPutWithContentValues extends PreparedPut<ContentValues, PutResult> {

    @NonNull private final ContentValues contentValues;

    PreparedPutWithContentValues(@NonNull StorIODb storIODb, @NonNull PutResolver<ContentValues> putResolver, @NonNull ContentValues contentValues) {
        super(storIODb, putResolver);
        this.contentValues = contentValues;
    }

    @NonNull @Override public PutResult executeAsBlocking() {
        final PutResult putResult = putResolver.performPut(
                storIODb,
                contentValues
        );

        putResolver.afterPut(contentValues, putResult);
        storIODb.internal().notifyAboutChanges(new Changes(putResult.affectedTables()));
        return putResult;
    }

    @NonNull @Override public Observable<PutResult> createObservable() {
        return Observable.create(new Observable.OnSubscribe<PutResult>() {
            @Override public void call(Subscriber<? super PutResult> subscriber) {
                final PutResult putResult = executeAsBlocking();

                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(putResult);
                    subscriber.onCompleted();
                }
            }
        });
    }

    public static class Builder {

        @NonNull private final StorIODb storIODb;
        @NonNull private final ContentValues contentValues;

        private PutResolver<ContentValues> putResolver;

        public Builder(@NonNull StorIODb storIODb, @NonNull ContentValues contentValues) {
            this.storIODb = storIODb;
            this.contentValues = contentValues;
        }

        @NonNull public Builder withPutResolver(@NonNull PutResolver<ContentValues> putResolver) {
            this.putResolver = putResolver;
            return this;
        }

        @NonNull public PreparedPutWithContentValues prepare() {
            return new PreparedPutWithContentValues(
                    storIODb,
                    putResolver,
                    contentValues
            );
        }
    }
}
