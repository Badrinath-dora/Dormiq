package com.dormiq.ui.viewmodel;

import com.dormiq.domain.repository.PaymentRepository;
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
public final class PaymentViewModel_Factory implements Factory<PaymentViewModel> {
  private final Provider<PaymentRepository> paymentRepositoryProvider;

  private final Provider<TenantRepository> tenantRepositoryProvider;

  public PaymentViewModel_Factory(Provider<PaymentRepository> paymentRepositoryProvider,
      Provider<TenantRepository> tenantRepositoryProvider) {
    this.paymentRepositoryProvider = paymentRepositoryProvider;
    this.tenantRepositoryProvider = tenantRepositoryProvider;
  }

  @Override
  public PaymentViewModel get() {
    return newInstance(paymentRepositoryProvider.get(), tenantRepositoryProvider.get());
  }

  public static PaymentViewModel_Factory create(
      Provider<PaymentRepository> paymentRepositoryProvider,
      Provider<TenantRepository> tenantRepositoryProvider) {
    return new PaymentViewModel_Factory(paymentRepositoryProvider, tenantRepositoryProvider);
  }

  public static PaymentViewModel newInstance(PaymentRepository paymentRepository,
      TenantRepository tenantRepository) {
    return new PaymentViewModel(paymentRepository, tenantRepository);
  }
}
