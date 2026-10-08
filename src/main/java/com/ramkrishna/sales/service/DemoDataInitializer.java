package com.ramkrishna.sales.service;

import com.ramkrishna.sales.entity.*;
import com.ramkrishna.sales.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.*;
import java.util.*;

@Configuration
public class DemoDataInitializer {
	private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
	@SuppressWarnings("unused")
	@Bean
	CommandLineRunner seedDemoData(UserRepository users, CustomerRepository customers, SiteRepository sites,
			RequestRepository requests, ActivityService activities, PasswordEncoder encoder) {
		return args -> {
			if (users.count() > 0) {
				log.info("Demo data initialization skipped because users already exist");
				return;
			}
			log.info("Initializing demo users and sales data");
			Instant now = Instant.now();
			UserEntity admin = new UserEntity("u1", "admin", encoder.encode("Admin@123"), "Admin", Enums.Role.ADMIN,
					true, now);
			UserEntity s1 = new UserEntity("u2", "sales1", encoder.encode("Sales@123"), "Sales 1",
					Enums.Role.SALES_USER, true, now);
			UserEntity s2 = new UserEntity("u3", "sales2", encoder.encode("Sales@123"), "Sales 2",
					Enums.Role.SALES_USER, true, now);
			UserEntity s3 = new UserEntity("u4", "sales3", encoder.encode("Sales@123"), "Sales 3",
					Enums.Role.SALES_USER, true, now);
			users.saveAll(List.of(admin, s1, s2, s3));
			String[][] cs = { { "c1", "Rahul Sharma", "", "INDIVIDUAL", "+91 98201 44521", "Mumbai" },
					{ "c2", "ABC Resorts", "ABC Resorts Pvt Ltd", "RESORT", "+91 98202 11789", "Alibaug" },
					{ "c3", "Design Studio Mumbai", "Design Studio Mumbai", "INTERIOR_DESIGNER", "+91 98190 23145",
							"Mumbai" },
					{ "c4", "XYZ Commercial Pvt Ltd", "XYZ Commercial Pvt Ltd", "COMMERCIAL", "+91 98921 55331",
							"Thane" },
					{ "c5", "Mehta Builders", "Mehta Builders", "BUILDER", "+91 98195 90871", "Navi Mumbai" },
					{ "c6", "Neha Kapoor", "", "INDIVIDUAL", "+91 98330 11229", "Mumbai" },
					{ "c7", "Harbour View Contractors", "", "CONTRACTOR", "+91 98765 22018", "Panvel" },
					{ "c8", "Green Valley Homes", "Green Valley Homes", "COMMERCIAL", "+91 99012 33441", "Pune" } };
			List<CustomerEntity> cl = new ArrayList<>();
			for (String[] x : cs)
				cl.add(new CustomerEntity(x[0], x[1], x[2].isBlank() ? null : x[2], Enums.CustomerType.valueOf(x[3]),
						x[4], null, null, null, x[5], "Maharashtra", null,
						now.minusSeconds((long) (Math.random() * 30) * 86400)));
			customers.saveAll(cl);
			Map<String, CustomerEntity> cm = new HashMap<>();
			cl.forEach(c -> cm.put(c.getId(), c));
			List<SiteEntity> sl = List.of(
					new SiteEntity("s1", cm.get("c2"), "Alibaug Resort", "Coastal Road", "Alibaug", "Maharashtra", null,
							"Amit Shah", "+91 98200 10001", null, now),
					new SiteEntity("s2", cm.get("c3"), "Bandra Residence", "Bandra West", "Mumbai", "Maharashtra", null,
							null, null, null, now),
					new SiteEntity("s3", cm.get("c4"), "Thane Office", "Ghodbunder Road", "Thane", "Maharashtra", null,
							null, null, null, now),
					new SiteEntity("s4", cm.get("c5"), "Panvel Villas", "New Panvel", "Panvel", "Maharashtra", null,
							null, null, null, now),
					new SiteEntity("s5", cm.get("c8"), "Pune Clubhouse", "Baner", "Pune", "Maharashtra", null, null,
							null, null, now));
			sites.saveAll(sl);
			Map<String, SiteEntity> sm = new HashMap<>();
			sl.forEach(s -> sm.put(s.getId(), s));
			String[] desc = { "uPVC sliding windows for a new residence.",
					"uPVC windows and doors for resort rooms and common areas.",
					"Full-height uPVC glazing requirement for a residential project.",
					"Office partition and window requirement.", "uPVC doors and casement windows for villas.",
					"Service visit for existing window hardware.", "Quotation request for premium sliding windows.",
					"Replacement of selected glass window units.", "Commercial project window package requirement.",
					"Site measurement and installation requirement.", "New enquiry for low-maintenance uPVC doors.",
					"Modification request for existing sliding track.", "Window requirement for clubhouse renovation.",
					"Follow-up on previously discussed project.", "uPVC casement windows for a sea-facing residence.",
					"New commercial cabin and partition requirement.", "Replacement doors for an existing customer.",
					"New enquiry from Google Ads for windows.", "Resort entrance door requirement.",
					"uPVC windows for a builder's new project." };
			Enums.RequestStatus[] sts = Enums.RequestStatus.values();
			Enums.Priority[] ps = Enums.Priority.values();
			Enums.RequestSource[] src = Enums.RequestSource.values();
			Enums.RequestType[] rt = Enums.RequestType.values();
			Enums.ProductCategory[] pc = Enums.ProductCategory.values();
			UserEntity[] su = { s1, s2, s3 };
			for (int i = 0; i < desc.length; i++) {
				long seq = requests.nextRequestSequence();
				RequestEntity r = new RequestEntity();
				CustomerEntity c = cl.get(i % cl.size());
				SiteEntity site = (i % 3 == 0 ? sm.get(List.of("s1", "s2", "s3", "s4", "s5").get(i % 5)) : null);
				UserEntity creator = su[i % 3];
				UserEntity ass = su[(i + 1) % 3];
				r.setInitial(UUID.randomUUID().toString(),
						"RK-" + now.atZone(ZoneId.systemDefault()).getYear() + "-" + String.format("%05d", seq), c,
						site, creator, ass, src[i % src.length], rt[i % rt.length], pc[i % pc.length], desc[i],
						(i % 5) + 1, ps[i % ps.length], sts[i % sts.length], LocalDate.now().plusDays(i + 2),
						i % 4 == 0 ? "Priority follow-up required." : null, now.minusSeconds((long) i * 86400));
				requests.save(r);
				activities.add(r, creator.getId(), Enums.ActivityType.REQUEST_CREATED, "created this request.", null,
						null, null);
				if (i % 4 == 0)
					activities.add(r, creator.getId(), Enums.ActivityType.COMMENT,
							"commented: “Customer follow-up required.”", null, null, null);
			}
			log.info("Demo data initialized users={} customers={} sites={} requests={}", 4, cl.size(), sl.size(),
					desc.length);
		};
	}
}
