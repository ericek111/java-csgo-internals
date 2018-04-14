package eu.lixko.csgoshared.natives;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public interface CLink extends Library {
	public static CLink INSTANCE = (CLink) Native.loadLibrary("c", CLink.class);

	public class Elf64_Phdr extends Structure implements Structure.ByReference {
		public int p_type; /* Segment type */
		public int p_flags;
		public long p_offset; /* Segment file offset */
		public long p_vaddr; /* Segment virtual address */
		public long p_paddr; /* Segment physical address */
		public long p_filesz; /* Segment size in file */
		public long p_memsz; /* Segment size in memory */
		public long p_align; /* Segment alignment, file & memory */

		public Elf64_Phdr(Pointer ptr) {
			super(ptr);
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("p_type", "p_flags", "p_offset", "p_vaddr", "p_paddr", "p_filesz", "p_memsz", "p_align");
		}
	}

	public class dl_phdr_info extends Structure {
		public long dlpi_addr; /* Base address of object */
		public String dlpi_name; /* (Null-terminated) name of object */
		// TODO: Implement actual array.
		public Elf64_Phdr dlpi_phdr; /* Pointer to array of ELF program headers for this object */
		public short dlpi_phnum; /* # of items in dlpi_phdr */

		/*
		 * Note: Following members were introduced after the first version of this
		 * structure was available. Check the SIZE argument passed to the
		 * dl_iterate_phdr callback to determine whether or not each later member is
		 * available.
		 */

		/* Incremented when a new object may have been added. */
		public long dlpi_adds;
		/* Incremented when an object may have been removed. */
		public long dlpi_subs;

		/*
		 * If there is a PT_TLS segment, its module ID as used in TLS relocations, else
		 * zero.
		 */
		public long dlpi_tls_modid;

		/*
		 * The address of the calling thread's instance of this module's PT_TLS segment,
		 * if it has one and it has been allocated in the calling thread, otherwise a
		 * null pointer.
		 */
		public long dlpi_tls_data;

		public dl_phdr_info(Pointer ptr) {
			super(ptr);
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("dlpi_addr", "dlpi_name", "dlpi_phdr", "dlpi_phnum", "dlpi_adds", "dlpi_subs", "dlpi_tls_modid", "dlpi_tls_data");
		}
	}

	interface dl_iterator extends Callback {
		// TODO: PointerByReference would've been a much nicer type for `data`
		// struct dl_phdr_info *info, size_t size, void *data
		int invoke(Pointer info, long size, long data);
	}

	int dl_iterate_phdr(dl_iterator callback, long base);

	public static dl_phdr_info findModule(String modulename) {
		final CLink.dl_phdr_info[] res = new CLink.dl_phdr_info[1];
		CLink.dl_iterator callback = new CLink.dl_iterator() {
			public int invoke(Pointer info, long size, long data) {
				CLink.dl_phdr_info phdr = new CLink.dl_phdr_info(info);
				phdr.read();

				if (phdr.dlpi_name.contains(modulename)) {
					res[0] = phdr;
					return 1;
				}
				return 0;
			}
		};
		
		CLink.INSTANCE.dl_iterate_phdr(callback, 0);
		return res[0];
	}
}