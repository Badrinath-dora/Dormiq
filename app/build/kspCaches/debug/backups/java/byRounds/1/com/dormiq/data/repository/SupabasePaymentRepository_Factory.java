package com.dormiq.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SupabasePaymentRepository_Factory implements Factory<SupabasePaymentRepository> {
  private final Provider<SupabaseClient> supabaseProvider;

  public SupabasePaymentRepository_Factory(Provider<SupabaseClient> supabaseProvider) {
    this.supabaseProvider = supabaseProvider;
  }

  @Override
  public SupabasePaymentRepository get() {
    return newInstance(supabaseProvider.get());
  }

  public static SupabasePaymentRepository_Factory create(
      Provider<SupabaseClient> supabaseProvider) {
    return new SupabasePaymentRepository_Factory(supabaseProvider);
  }

  public static SupabasePaymentRepository newInstance(SupabaseClient supabase) {
    return new SupabasePaymentRepository(supabase);
  }
}
