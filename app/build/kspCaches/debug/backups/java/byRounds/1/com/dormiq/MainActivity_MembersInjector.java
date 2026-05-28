package com.dormiq;

import com.dormiq.data.manager.NetworkConnectivityObserver;
import com.dormiq.domain.repository.AuthRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<NetworkConnectivityObserver> connectivityObserverProvider;

  public MainActivity_MembersInjector(Provider<AuthRepository> authRepositoryProvider,
      Provider<NetworkConnectivityObserver> connectivityObserverProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.connectivityObserverProvider = connectivityObserverProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<AuthRepository> authRepositoryProvider,
      Provider<NetworkConnectivityObserver> connectivityObserverProvider) {
    return new MainActivity_MembersInjector(authRepositoryProvider, connectivityObserverProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectAuthRepository(instance, authRepositoryProvider.get());
    injectConnectivityObserver(instance, connectivityObserverProvider.get());
  }

  @InjectedFieldSignature("com.dormiq.MainActivity.authRepository")
  public static void injectAuthRepository(MainActivity instance, AuthRepository authRepository) {
    instance.authRepository = authRepository;
  }

  @InjectedFieldSignature("com.dormiq.MainActivity.connectivityObserver")
  public static void injectConnectivityObserver(MainActivity instance,
      NetworkConnectivityObserver connectivityObserver) {
    instance.connectivityObserver = connectivityObserver;
  }
}
