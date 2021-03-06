/**
 * The MIT License
 * Copyright (c) 2016 Regis Leray
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package factoryduke;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import factoryduke.generators.Generators;
import factoryduke.generators.SequenceValuesGenerator;
import model.Address;
import model.Role;
import model.User;

public class ManualFactoryDukeTest {

	@Before
	public void defineManualFactory(){
		FactoryDuke.reset();

		FactoryDuke.define(User.class, u -> {
			u.setLastName("Scott");
			u.setName("Malcom");
			u.setRole(Role.USER);
		});

		FactoryDuke.define(User.class, "admin_user", u -> {
			u.setLastName("John");
			u.setName("Malcom");
			u.setRole(Role.ADMIN);
		});

		FactoryDuke.define(User.class, "user_with_address", () -> {
			User u = FactoryDuke.build(User.class).toOne();

			Address adress = new Address();
			adress.setCity("MTL");
			adress.setStreet("prince street");
			u.setAddr(adress);
			return u;
		});

		FactoryDuke.define(User.class, "user_with_fr_address", u -> {
			u.setLastName("Scott");
			u.setName("Malcom");
			u.setRole(Role.USER);

			u.setAddr(FactoryDuke.build(Address.class, "address_in_fr").toOne());
		});

		FactoryDuke.define(Address.class, "address_in_fr", a -> {
			a.setCity("Paris");
			a.setStreet("rue d'avignon");
			a.setCountry("France");
		});
	}

	@Test
	public void defaultUser(){
		User defaultUser = FactoryDuke.build(User.class).toOne();
		assertThat(defaultUser).isNotNull();
	}

	@Test
	public void defaultUserWithOverride(){
		User user = FactoryDuke.build(User.class, u -> u.setName("toto")).toOne();
		assertThat(user).isNotNull().extracting(User::getName).contains("toto");
	}

	@Test
	public void adminUser(){
		User user = FactoryDuke.build(User.class, "admin_user").toOne();
		assertThat(user).isNotNull().extracting(User::getRole).contains(Role.ADMIN);
	}

	@Test
	public void defaultUserWithAddr(){
		User user = FactoryDuke.build(User.class, "user_with_address").toOne();
		assertThat(user).isNotNull().hasFieldOrPropertyWithValue("addr.city", "MTL");
	}

	@Test
	public void defaultUserWithAddrWithOverride(){
		User user = FactoryDuke.build(User.class, "user_with_fr_address").toOne();
		assertThat(user).isNotNull().hasFieldOrPropertyWithValue("addr.city", "Paris");
	}

	@Test
	public void adminUserWithFrAddr(){
		User user = FactoryDuke.build(User.class, "user_with_fr_address", u -> u.setRole(Role.ADMIN)).toOne();

		assertThat(user).isNotNull()
				.hasFieldOrPropertyWithValue("role", Role.ADMIN)
				.hasFieldOrPropertyWithValue("addr.city", "Paris");
	}

	@Test
	public void repeat_with_generators(){
		FactoryDuke.reset();

		SequenceValuesGenerator<Long> ids = Generators.values(1L, 2L, 3L);
		SequenceValuesGenerator<String> names = Generators.values("Scott", "John", "Malcom");

		FactoryDuke.define(User.class, u -> {
			u.setId(ids.nextValue());
			u.setName(names.nextValue());
		});

		List<User> users = FactoryDuke.build(User.class).times(3).toList();

		assertThat(users).hasSize(3).extracting(User::getId, User::getName)
				.containsExactly(tuple(1L, "Scott"), tuple(2L, "John"), tuple(3L, "Malcom"));
	}

	@Test
	public void repeat_no_generators(){
		FactoryDuke.reset();

		FactoryDuke.define(User.class, u -> {
			u.setId(1L);
			u.setName("James");
		});

		List<User> users = FactoryDuke.build(User.class).times(3).toList();

		assertThat(users).hasSize(3).extracting(User::getId, User::getName)
				.containsExactly(tuple(1L, "James"), tuple(1L, "James"), tuple(1L, "James"));
	}
}
