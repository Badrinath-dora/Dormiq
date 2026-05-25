package com.dormiq.data.manager;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class ActiveHostelManager_Factory implements Factory<ActiveHostelManager> {
  private final Provider<Context> contextProvider;

  public ActiveHostelManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ActiveHostelManager get() {
    return newInstance(contextProvider.get());
  }

  public static ActiveHostelManager_Factory create(Provider<Context> contextProvider) {
    return new ActiveHostelManager_Factory(contextProvider);
  }

  public static ActiveHostelManager newInstance(Context context) {
    return new ActiveHostelManager(context);
  }
}
