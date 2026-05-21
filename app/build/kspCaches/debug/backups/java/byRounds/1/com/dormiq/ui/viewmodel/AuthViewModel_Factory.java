package com.dormiq.ui.viewmodel;

import com.dormiq.domain.repository.AuthRepository;
import com.dormiq.domain.repository.TenantRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<TenantRepository> tenantRepositoryProvider;

  public AuthViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<TenantRepository> tenantRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.tenantRepositoryProvider = tenantRepositoryProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(authRepositoryProvider.get(), tenantRepositoryProvider.get());
  }

  public static AuthViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<TenantRepository> tenantRepositoryProvider) {
    return new AuthViewModel_Factory(authRepositoryProvider, tenantRepositoryProvider);
  }

  public static AuthViewModel newInstance(AuthRepository authRepository,
      TenantRepository tenantRepository) {
    return new AuthViewModel(authRepository, tenantRepository);
  }
}
