package com.babilonia.data.mapper

import com.babilonia.data.db.model.ContactDto
import com.babilonia.data.network.model.json.ContactJson
import com.babilonia.domain.model.Contact
import javax.inject.Inject

class ContactMapper @Inject constructor() : Mapper<ContactDto, ContactJson, Contact> {

    override fun mapRemoteToLocal(from: ContactJson): ContactDto {
        return mapDomainToLocal(mapRemoteToDomain(from))
    }

    override fun mapLocalToRemote(from: ContactDto): ContactJson {
        return mapDomainToRemote(mapLocalToDomain(from))
    }

    override fun mapDomainToLocal(from: Contact): ContactDto {
        return ContactDto().apply {
            contactName = from.contactName
            contactEmail = from.contactEmail
            contactPhone = from.contactPhone
        }
    }

    override fun mapDomainToRemote(from: Contact): ContactJson {
        return ContactJson().apply {
            contactName = from.contactName
            contactEmail = from.contactEmail
            contactPhone = from.contactPhone
        }
    }

    override fun mapLocalToDomain(from: ContactDto): Contact = Contact(
        from.contactName,
        from.contactEmail,
        from.contactPhone
    )

    override fun mapRemoteToDomain(from: ContactJson): Contact = Contact(
        from.contactName,
        from.contactEmail,
        from.contactPhone
    )
}