//
// Originally generated from a JAXB schema; the schema was dropped and this class
// is now maintained by hand. It carries JAXB annotations for the products XML
// mapping and is Serializable so it can live in a Faces session-scoped bean.
//

package shopping.business.generated;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * A product in the catalogue: id, name, price, quantity and a description.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "product", propOrder = {
    "description",
    "id",
    "name",
    "price",
    "quantity"
})
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String description;
    protected String id;
    protected String name;
    protected BigDecimal price;
    protected int quantity;

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal value) {
        this.price = value;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int value) {
        this.quantity = value;
    }

    @Override
    public String toString() {
        return "Product[id=" + id + ", name=" + name + ", price=" + price
                + ", quantity=" + quantity + ", description=" + description + "]";
    }
}
