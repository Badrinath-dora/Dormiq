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
public final class SupabaseHostelRepository_Factory implements Factory<SupabaseHostelRepository> {
  private final Provider<SupabaseClient> supabaseProvider;

  public SupabaseHostelRepository_Factory(Provider<SupabaseClient> supabaseProvider) {
    this.supabaseProvider = supabaseProvider;
  }

  @Override
  public SupabaseHostelRepository get() {
    return newInstance(supabaseProvider.get());
  }

  public static SupabaseHostelRepository_Factory create(Provider<SupabaseClient> supabaseProvider) {
    return new SupabaseHostelRepository_Factory(supabaseProvider);
  }

  public static SupabaseHostelRepository newInstance(SupabaseClient supabase) {
    return new SupabaseHostelRepository(supabase);
  }
}
