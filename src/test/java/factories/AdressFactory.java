package factories;

import factoryduke.TFactory;
import factoryduke.FactoryDuke;
import model.Address;

public class AdressFactory implements TFactory {
	@Override
	public void define() {
		FactoryDuke.define(Address.class, "address_in_fr", a -> {
			a.setCity("Paris");
			a.setStreet("rue d'avignon");
			a.setCountry("France");
		});

		FactoryDuke.define(Address.class, "address_in_us", a -> {
			a.setCity("New York");
			a.setStreet("manhattan avenue");
			a.setCountry("United States");
		});

	}
}
