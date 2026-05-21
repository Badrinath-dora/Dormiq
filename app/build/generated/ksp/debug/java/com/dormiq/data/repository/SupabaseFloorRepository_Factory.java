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
public final class SupabaseFloorRepository_Factory implements Factory<SupabaseFloorRepository> {
  private final Provider<SupabaseClient> supabaseProvider;

  public SupabaseFloorRepository_Factory(Provider<SupabaseClient> supabaseProvider) {
    this.supabaseProvider = supabaseProvider;
  }

  @Override
  public SupabaseFloorRepository get() {
    return newInstance(supabaseProvider.get());
  }

  public static SupabaseFloorRepository_Factory create(Provider<SupabaseClient> supabaseProvider) {
    return new SupabaseFloorRepository_Factory(supabaseProvider);
  }

  public static SupabaseFloorRepository newInstance(SupabaseClient supabase) {
    return new SupabaseFloorRepository(supabase);
  }
}
