package com.dormiq.data.repository;

import com.dormiq.data.manager.ActiveHostelManager;
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
public final class SupabaseAuthRepository_Factory implements Factory<SupabaseAuthRepository> {
  private final Provider<SupabaseClient> supabaseClientProvider;

  private final Provider<ActiveHostelManager> activeHostelManagerProvider;

  public SupabaseAuthRepository_Factory(Provider<SupabaseClient> supabaseClientProvider,
      Provider<ActiveHostelManager> activeHostelManagerProvider) {
    this.supabaseClientProvider = supabaseClientProvider;
    this.activeHostelManagerProvider = activeHostelManagerProvider;
  }

  @Override
  public SupabaseAuthRepository get() {
    return newInstance(supabaseClientProvider.get(), activeHostelManagerProvider.get());
  }

  public static SupabaseAuthRepository_Factory create(
      Provider<SupabaseClient> supabaseClientProvider,
      Provider<ActiveHostelManager> activeHostelManagerProvider) {
    return new SupabaseAuthRepository_Factory(supabaseClientProvider, activeHostelManagerProvider);
  }

  public static SupabaseAuthRepository newInstance(SupabaseClient supabaseClient,
      ActiveHostelManager activeHostelManager) {
    return new SupabaseAuthRepository(supabaseClient, activeHostelManager);
  }
}
