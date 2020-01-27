package org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities

data class ResourceLinkEntity(
    var id: Int,
    var resourceContentFk: Int,
    var contentFk: Int?,
    var collectionFk: Int?,
    var dublinCoreFk: Int
)

/** Build a [ContentEntity] [ResourceLinkEntity] with id=0. */
fun resourceLinkEntity(resource: ContentEntity, content: ContentEntity, metadata: ResourceMetadataEntity) =
    ResourceLinkEntity(
        id = 0,
        resourceContentFk = resource.id,
        contentFk = content.id,
        collectionFk = null,
        dublinCoreFk = metadata.id
    )

/** Build a [CollectionEntity] [ResourceLinkEntity] with id=0. */
fun resourceLinkEntity(resource: ContentEntity, collection: CollectionEntity, metadata: ResourceMetadataEntity) =
    ResourceLinkEntity(
        id = 0,
        resourceContentFk = resource.id,
        contentFk = null,
        collectionFk = collection.id,
        dublinCoreFk = metadata.id
    )
