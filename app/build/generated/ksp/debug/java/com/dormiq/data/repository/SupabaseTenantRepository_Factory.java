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
public final class SupabaseTenantRepository_Factory implements Factory<SupabaseTenantRepository> {
  private final Provider<SupabaseClient> supabaseProvider;

  private final Provider<ActiveHostelManager> activeHostelManagerProvider;

  public SupabaseTenantRepository_Factory(Provider<SupabaseClient> supabaseProvider,
      Provider<ActiveHostelManager> activeHostelManagerProvider) {
    this.supabaseProvider = supabaseProvider;
    this.activeHostelManagerProvider = activeHostelManagerProvider;
  }

  @Override
  public SupabaseTenantRepository get() {
    return newInstance(supabaseProvider.get(), activeHostelManagerProvider.get());
  }

  public static SupabaseTenantRepository_Factory create(Provider<SupabaseClient> supabaseProvider,
      Provider<ActiveHostelManager> activeHostelManagerProvider) {
    return new SupabaseTenantRepository_Factory(supabaseProvider, activeHostelManagerProvider);
  }

  public static SupabaseTenantRepository newInstance(SupabaseClient supabase,
      ActiveHostelManager activeHostelManager) {
    return new SupabaseTenantRepository(supabase, activeHostelManager);
  }
}
