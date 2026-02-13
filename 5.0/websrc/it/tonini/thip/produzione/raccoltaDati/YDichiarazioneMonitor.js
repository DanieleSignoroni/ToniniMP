var oldRilevDatiPrdTSOL = RilevDatiPrdTSOL;

RilevDatiPrdTSOL = function() {
	oldRilevDatiPrdTSOL();

	$(function() {
		var table = $('#extraTable');

		if (!table.length) return;
		if ($.fn.DataTable.isDataTable(table)) return;

		table.DataTable({
			pageLength: 25,
			stateSave: false,
			stateLoadParams: function(settings, data) {
				data.order = [[2, 'asc'], [8, 'asc']];
			},
			order: [[2, 'asc'], [8, 'asc']],
			autoWidth: false,
			language: { search: "Filtra:", zeroRecords: "Nessun risultato" },

			initComplete: function() {
				const api = this.api();

				api.columns().every(function() {
					const column = this;
					if(column.index() == 0 || (column.index() == column.row().table().columns().data().length -1)) return;
					const footerCell = column.footer();
					if (!footerCell) return;

					const title = footerCell.textContent.trim();

					const input = document.createElement('input');
					input.classList.add('input-filtri');
					input.placeholder = title;

					footerCell.replaceChildren(input);

					input.addEventListener('keyup', function() {
						const val = input.value;
						if (column.search() !== val) {
							column.search(val).draw();
						}
					});
				});
			}
		});
	});
};
