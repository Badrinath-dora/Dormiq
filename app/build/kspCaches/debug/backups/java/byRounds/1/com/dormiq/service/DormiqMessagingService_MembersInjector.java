package com.dormiq.service;

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
public final class DormiqMessagingService_MembersInjector implements MembersInjector<DormiqMessagingService> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public DormiqMessagingService_MembersInjector(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  public static MembersInjector<DormiqMessagingService> create(
      Provider<AuthRepository> authRepositoryProvider) {
    return new DormiqMessagingService_MembersInjector(authRepositoryProvider);
  }

  @Override
  public void injectMembers(DormiqMessagingService instance) {
    injectAuthRepository(instance, authRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.dormiq.service.DormiqMessagingService.authRepository")
  public static void injectAuthRepository(DormiqMessagingService instance,
      AuthRepository authRepository) {
    instance.authRepository = authRepository;
  }
}
