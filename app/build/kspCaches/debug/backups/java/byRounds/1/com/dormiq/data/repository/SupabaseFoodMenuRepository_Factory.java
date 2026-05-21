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
public final class SupabaseFoodMenuRepository_Factory implements Factory<SupabaseFoodMenuRepository> {
  private final Provider<SupabaseClient> supabaseProvider;

  public SupabaseFoodMenuRepository_Factory(Provider<SupabaseClient> supabaseProvider) {
    this.supabaseProvider = supabaseProvider;
  }

  @Override
  public SupabaseFoodMenuRepository get() {
    return newInstance(supabaseProvider.get());
  }

  public static SupabaseFoodMenuRepository_Factory create(
      Provider<SupabaseClient> supabaseProvider) {
    return new SupabaseFoodMenuRepository_Factory(supabaseProvider);
  }

  public static SupabaseFoodMenuRepository newInstance(SupabaseClient supabase) {
    return new SupabaseFoodMenuRepository(supabase);
  }
}
