package de.frosner.broccoli.templates

import javax.inject.Singleton

import com.google.inject.{AbstractModule, Provides}
import de.frosner.broccoli.BroccoliConfiguration
import de.frosner.broccoli.signal.UnixSignalManager
import net.codingwell.scalaguice.ScalaModule

/**
  * Provide a template source from the Play configuration
  */
class TemplateModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {}

  /**
    * Provide the template source.
    *
    * @param config The Play configuration
    * @return The template source configured from the Play configuration
    */
  @Provides
  @Singleton
  def provideTemplateSource(config: BroccoliConfiguration, signalManager: UnixSignalManager): TemplateSource =
    new SignalRefreshedTemplateSource(
      new CachedTemplateSource(new DirectoryTemplateSource(config.templates.path)),
      signalManager
    )
}
