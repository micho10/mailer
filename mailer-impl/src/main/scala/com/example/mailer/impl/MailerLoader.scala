package com.example.mailer.impl

import com.example.mailer.api.MailerService
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.api.{Descriptor, ServiceLocator}
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

/**
  * A Play application loader for Lagom.
  *
  * Scala Lagom applications should provide a subclass of this to create their application, and configure it in their
  * `application.conf` file using:
  *
  * ```
  * play.application.loader = com.example.MyApplicationLoader
  * ```
  *
  * This class provides an abstraction over Play's application loader that provides Lagom specific functionality.
  */
class MailerLoader extends LagomApplicationLoader {

  /**
    * Load a Lagom application for production.
    *
    * This should mix in a production service locator implementation, and anything else specific to production, to
    * an application provided subclass of [[LagomApplication]]. It will be invoked to load the application in production.
    *
    * @param context The Lagom application context.
    * @return A Lagom application.
    */
  override def load(context: LagomApplicationContext): LagomApplication =
    new MailerApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  /**
    * Load a Lagom application for development.
    *
    * This should mix in [[LagomDevModeComponents]] with an application provided subclass of [[LagomApplication]], such
    * that the service locator used will be the dev mode service locator, and so that services get registered with the
    * dev mode service registry.
    *
    * @param context The Lagom application context.
    * @return A lagom application.
    */
  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new MailerApplication(context) with LagomDevModeComponents

  /**
    * This method allows tooling, such as ConductR, to discover the service (if any) offered by this application.
    *
    * This will be used to generate configuration regarding ACLs and service name for production deployment.
    */
  override def describeService: Option[Descriptor] = Some(readDescriptor[MailerService])
}



/**
  * A Lagom application.
  *
  * It subclasses [[LagomApplication]] in order to wire together a Lagom application.
  *
  * This includes the Lagom server components (which builds and provides the Lagom router) as well as the Lagom
  * service client components (which allows implementing Lagom service clients from Lagom service descriptors).
  *
  * There are two abstract defs that must be implemented, one is [[LagomServerComponents.lagomServer]], the other
  * is [[LagomServiceClientComponents.serviceLocator]]. Typically, the `lagomServer` component will be implemented by
  * an abstract subclass of [[LagomApplication]], and will bind all the services that this Lagom application provides.
  * Meanwhile, the `serviceLocator` component will be provided by mixing in a service locator components trait in
  * [[LagomApplicationLoader]], which trait is mixed in will vary depending on whether the application is being loaded
  * for production or for development.
  *
  * @param context The application context.
  */
abstract class MailerApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  // Bind the services that this server provides
  override lazy val lagomServer: LagomServer =
    serverFor[MailerService](bindService[MailerService].to(wire[MailerServiceImpl]).service)

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = MailerSerializerRegistry

  // Register the Mailer persistent entity
  persistentEntityRegistry.register(wire[MailEntity])
}
