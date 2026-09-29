/* =========================================================
   WAREHOUSE SLOT
   SMART WAREHOUSE MANAGEMENT
   Frontend API Controller
========================================================= */

const API = "/api";


/* =========================================================
   APPLICATION STATE
========================================================= */

const state = {

    products: [],
    bins: [],
    stocks: [],
    orders: [],
    pickLists: []

};


/* =========================================================
   DOM HELPER
========================================================= */

const $ = id => document.getElementById(id);


/* =========================================================
   ESCAPE HTML
========================================================= */

function escapeHtml(value) {

    if (
        value === null ||
        value === undefined
    ) {
        return "";
    }

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}


/* =========================================================
   MESSAGE
========================================================= */

function showMessage(message) {

    const box = $("message");

    if (!box) {

        alert(message);

        return;
    }

    box.textContent = message;

    box.classList.add("show");

    clearTimeout(showMessage.timer);

    showMessage.timer = setTimeout(() => {

        box.classList.remove("show");

    }, 3500);

}


/* =========================================================
   API HELPER
========================================================= */

async function api(path, options = {}) {

    const response = await fetch(

        API + path,

        {

            ...options,

            headers: {

                Accept:
                    "application/json",

                ...(options.body
                    ? {
                        "Content-Type":
                            "application/json"
                    }
                    : {}),

                ...options.headers

            },

            body:

                options.body
                    ? JSON.stringify(options.body)
                    : undefined

        }

    );


    const text =
        (await response.text()).trim();


    if (!response.ok) {

        let message =
            text ||
            `Request failed (${response.status})`;


        try {

            const data =
                JSON.parse(text);

            message =
                data.message ||
                data.error ||
                message;

        } catch (_) {

            // plain text error

        }


        const error =
            new Error(message);

        error.status =
            response.status;

        throw error;

    }


    if (!text) {

        return null;

    }


    try {

        return JSON.parse(text);

    } catch (_) {

        throw new Error(
            "Backend returned invalid JSON: " +
            text.substring(0, 200)
        );

    }

}


/* =========================================================
   DATA HELPERS
========================================================= */

const toList = result =>

    Array.isArray(result)
        ? result
        : (
            result &&
            Array.isArray(result.data)
                ? result.data
                : []
        );


function setText(id, value) {

    const element = $(id);

    if (element) {

        element.textContent = value;

    }

}


const formatDate = value =>

    value
        ? new Date(value).toLocaleString()
        : "-";


function emptyRow(cols, text) {

    return `
        <tr>
            <td colspan="${cols}" class="empty-row">
                ${escapeHtml(text)}
            </td>
        </tr>
    `;

}


function renderTable(
    bodyId,
    rows,
    cols,
    emptyText,
    rowHtml
) {

    const body = $(bodyId);

    if (!body) return;


    body.innerHTML =

        rows.length

            ? rows.map(rowHtml).join("")

            : emptyRow(
                cols,
                emptyText
            );

}


/* =========================================================
   SELECT DROPDOWN
========================================================= */

function fillSelect(
    id,
    items,
    label
) {

    const select = $(id);

    if (!select) return;


    const previous =
        select.value;


    select.innerHTML =

        `<option value="">
            ${escapeHtml(label)}
        </option>` +

        items.map(item =>

            `<option value="${escapeHtml(item.value)}">
                ${escapeHtml(item.text)}
            </option>`

        ).join("");


    if (
        previous &&
        items.some(
            item =>
                String(item.value) ===
                previous
        )
    ) {

        select.value =
            previous;

    }

}


/* =========================================================
   RUN API ACTION
========================================================= */

async function run(
    action,
    success,
    failure
) {

    try {

        await action();

        if (success) {

            showMessage(success);

        }

    } catch (error) {

        console.error(error);

        showMessage(
            `${failure}: ${error.message}`
        );

    }

}


/* =========================================================
   NAVIGATION
========================================================= */

function showSection(
    sectionId,
    button = null
) {

    document
        .querySelectorAll(".section")
        .forEach(section => {

            section.classList.remove(
                "active"
            );

        });


    $(sectionId)
        ?.classList.add("active");


    document
        .querySelectorAll(".tab-button")
        .forEach(btn => {

            btn.classList.remove(
                "active"
            );

        });


    (
        button ||
        document.querySelector(
            `[data-tab="${sectionId}"]`
        )
    )
        ?.classList.add("active");


    const loaders = {

        dashboardSection:
        refreshAllData,

        productsSection:
        loadProducts,

        binsSection:
        loadBins,

        stockSection:
            () => Promise.all([
                loadProducts(),
                loadStocks(),
                loadBins()
            ]),

        ordersSection:
            () => Promise.all([
                loadProducts(),
                loadOrders()
            ]),

        pickListSection:
            () => Promise.all([
                loadOrders(),
                loadPickLists()
            ])

    };


    loaders[sectionId]?.();

}


/* =========================================================
   PRODUCTS
========================================================= */

async function loadProducts() {

    await run(

        async () => {

            state.products =
                toList(
                    await api("/products")
                );


            setText(
                "productCount",
                state.products.length
            );


            setText(
                "productTableCount",
                state.products.length
            );


            displayProducts();


            const options =
                state.products.map(p => ({

                    value: p.id,

                    text:
                        `${p.name} (${p.sku})`

                }));


            fillSelect(
                "stockProduct",
                options,
                "Select product"
            );


            fillSelect(
                "orderItemProduct",
                options,
                "Select product"
            );

        },

        null,

        "Could not load products"

    );

}


/* =========================================================
   DISPLAY PRODUCTS
========================================================= */

function displayProducts() {

    renderTable(

        "productsTableBody",

        state.products,

        5,

        "No products yet. Add your first product above.",

        p => {

            const velocity =
                p.velocity || "";


            return `

                <tr>

                    <td>
                        ${p.id ?? ""}
                    </td>

                    <td>
                        <strong>
                            ${escapeHtml(p.name)}
                        </strong>
                    </td>

                    <td>
                        ${escapeHtml(p.sku)}
                    </td>

                    <td>

                        <span class="
                            badge
                            badge-${velocity.toLowerCase()}
                        ">

                            ${escapeHtml(velocity)}

                        </span>

                    </td>

                    <td>

                        <button
                            class="edit-button"
                            onclick="editProduct(${p.id})">

                            Edit

                        </button>

                        <button
                            class="delete-button"
                            onclick="deleteProduct(${p.id})">

                            Delete

                        </button>

                    </td>

                </tr>

            `;

        }

    );

}


/* =========================================================
   ADD PRODUCT
========================================================= */

async function addProduct(event) {

    event.preventDefault();


    const form =
        event.target;


    const product = {

        name:
            $("productName")
                .value
                .trim(),

        sku:
            $("productSku")
                .value
                .trim(),

        velocity:
        $("productVelocity")
            .value

    };


    if (
        !product.name ||
        !product.sku ||
        !product.velocity
    ) {

        return showMessage(
            "Please fill all product fields"
        );

    }


    await run(

        async () => {

            await api(

                "/products",

                {
                    method:
                        "POST",

                    body:
                    product
                }

            );


            form.reset();

            await loadProducts();

        },

        "Product added successfully",

        "Could not add product"

    );

}


/* =========================================================
   EDIT PRODUCT
========================================================= */

async function editProduct(id) {

    const product =
        state.products.find(
            p =>
                Number(p.id) ===
                Number(id)
        );


    if (!product) return;


    const name =
        prompt(
            "Product name:",
            product.name
        );


    if (name === null) return;


    const sku =
        prompt(
            "SKU:",
            product.sku
        );


    if (sku === null) return;


    const velocity =
        prompt(
            "Velocity (FAST / MEDIUM / SLOW):",
            product.velocity
        );


    if (velocity === null) return;


    await run(

        async () => {

            await api(

                `/products/${id}`,

                {

                    method:
                        "PUT",

                    body: {

                        name:
                            name.trim(),

                        sku:
                            sku.trim(),

                        velocity:
                            velocity
                                .trim()
                                .toUpperCase()

                    }

                }

            );


            await loadProducts();

        },

        "Product updated successfully",

        "Could not update product"

    );

}


/* =========================================================
   DELETE PRODUCT
========================================================= */

async function deleteProduct(id) {

    if (
        !confirm(
            "Are you sure you want to delete this product?"
        )
    ) return;


    await run(

        async () => {

            await api(

                `/products/${id}`,

                {
                    method:
                        "DELETE"
                }

            );


            await Promise.all([

                loadProducts(),

                loadStocks()

            ]);

        },

        "Product deleted successfully",

        "Could not delete product"

    );

}


/* =========================================================
   BINS
========================================================= */

async function loadBins() {

    await run(

        async () => {

            state.bins =
                toList(
                    await api("/bins")
                );


            setText(
                "binCount",
                state.bins.length
            );


            displayBins();

        },

        null,

        "Could not load bins"

    );

}


/* =========================================================
   DISPLAY BINS
========================================================= */

function displayBins() {

    renderTable(

        "binsTableBody",

        state.bins,

        7,

        "No bins yet. Add your first bin above.",

        b => `

            <tr>

                <td>
                    ${b.id ?? ""}
                </td>

                <td>
                    <strong>
                        ${escapeHtml(b.binCode)}
                    </strong>
                </td>

                <td>
                    ${escapeHtml(b.zone)}
                </td>

                <td>
                    ${b.distanceFromDispatch ?? 0}
                </td>

                <td>
                    ${b.capacity ?? 0}
                </td>

                <td>
                    ${b.currentQuantity ?? 0}
                </td>

                <td>

                    <button
                        class="edit-button"
                        onclick="editBin(${b.id})">

                        Edit

                    </button>

                    <button
                        class="delete-button"
                        onclick="deleteBin(${b.id})">

                        Delete

                    </button>

                </td>

            </tr>

        `

    );

}


/* =========================================================
   ADD BIN
========================================================= */

async function addBin(event) {

    event.preventDefault();


    const form =
        event.target;


    const bin = {

        binCode:
            $("binCode")
                .value
                .trim(),

        zone:
            $("binZone")
                .value
                .trim(),

        distanceFromDispatch:
            Number(
                $("binDistance")
                    .value
            ),

        capacity:
            Number(
                $("binCapacity")
                    .value
            ),

        currentQuantity:
            0

    };


    if (
        !bin.binCode ||
        !bin.zone ||
        bin.distanceFromDispatch < 0 ||
        bin.capacity <= 0
    ) {

        return showMessage(
            "Enter valid bin details"
        );

    }


    await run(

        async () => {

            await api(

                "/bins",

                {
                    method:
                        "POST",

                    body:
                    bin
                }

            );


            form.reset();

            await loadBins();

        },

        "Bin added successfully",

        "Could not add bin"

    );

}


/* =========================================================
   EDIT BIN
========================================================= */

async function editBin(id) {

    const bin =
        state.bins.find(
            b =>
                Number(b.id) ===
                Number(id)
        );


    if (!bin) return;


    const binCode =
        prompt(
            "Bin code:",
            bin.binCode
        );


    if (binCode === null) return;


    const zone =
        prompt(
            "Zone:",
            bin.zone
        );


    if (zone === null) return;


    const distance =
        prompt(
            "Distance from dispatch:",
            bin.distanceFromDispatch
        );


    if (distance === null) return;


    const capacity =
        prompt(
            "Capacity:",
            bin.capacity
        );


    if (capacity === null) return;


    await run(

        async () => {

            await api(

                `/bins/${id}`,

                {

                    method:
                        "PUT",

                    body: {

                        binCode:
                            binCode.trim(),

                        zone:
                            zone.trim(),

                        distanceFromDispatch:
                            Number(distance),

                        capacity:
                            Number(capacity),

                        currentQuantity:
                            Number(
                                bin.currentQuantity ||
                                0
                            )

                    }

                }

            );


            await loadBins();

        },

        "Bin updated successfully",

        "Could not update bin"

    );

}


/* =========================================================
   DELETE BIN
========================================================= */

async function deleteBin(id) {

    if (
        !confirm(
            "Are you sure you want to delete this bin?"
        )
    ) return;


    await run(

        async () => {

            await api(

                `/bins/${id}`,

                {
                    method:
                        "DELETE"
                }

            );


            await loadBins();

        },

        "Bin deleted successfully",

        "Could not delete bin"

    );

}


/* =========================================================
   STOCK
========================================================= */

async function loadStocks() {

    await run(

        async () => {

            state.stocks =
                toList(
                    await api("/stocks")
                );


            displayStocks();

        },

        null,

        "Could not load stock"

    );

}


/* =========================================================
   DISPLAY STOCK
========================================================= */

function displayStocks() {

    renderTable(

        "stockTableBody",

        state.stocks,

        5,

        "No stock received yet.",

        s => `

            <tr>

                <td>
                    ${s.id ?? ""}
                </td>

                <td>
                    ${escapeHtml(
            s.product?.name ||
            "Unknown"
        )}
                </td>

                <td>
                    <strong>
                        ${escapeHtml(
            s.bin?.binCode ||
            "Unknown"
        )}
                    </strong>
                </td>

                <td>
                    ${s.quantity ?? 0}
                </td>

                <td>

                    <button
                        class="delete-button"
                        onclick="deleteStock(${s.id})">

                        Delete

                    </button>

                </td>

            </tr>

        `

    );

}


/* =========================================================
   RECEIVE STOCK
========================================================= */

async function receiveStock(event) {

    event.preventDefault();


    const form =
        event.target;


    const productId =
        Number(
            $("stockProduct")
                .value
        );


    const quantity =
        Number(
            $("stockQuantity")
                .value
        );


    if (
        !productId ||
        quantity <= 0
    ) {

        return showMessage(
            "Select a product and enter a valid quantity"
        );

    }


    await run(

        async () => {

            const result =
                await api(

                    "/stocks/receive",

                    {

                        method:
                            "POST",

                        body: {
                            productId,
                            quantity
                        }

                    }

                );


            form.reset();


            await Promise.all([

                loadStocks(),

                loadBins()

            ]);


            showMessage(
                `Stock assigned to ${
                    result?.bin?.binCode ||
                    "selected bin"
                }`
            );

        },

        null,

        "Could not receive stock"

    );

}


/* =========================================================
   DELETE STOCK
========================================================= */

async function deleteStock(id) {

    if (
        !confirm(
            "Delete this stock record?"
        )
    ) return;


    await run(

        async () => {

            await api(

                `/stocks/${id}`,

                {
                    method:
                        "DELETE"
                }

            );


            await Promise.all([

                loadStocks(),

                loadBins()

            ]);

        },

        "Stock deleted successfully",

        "Could not delete stock"

    );

}


/* =========================================================
   ORDERS
========================================================= */

async function loadOrders() {

    await run(

        async () => {

            state.orders =
                toList(
                    await api("/orders")
                );


            setText(
                "orderCount",
                state.orders.length
            );


            displayOrders();


            const options =
                state.orders.map(o => ({

                    value:
                    o.id,

                    text:
                        `${o.orderNumber} - ${o.status}`

                }));


            fillSelect(
                "orderItemOrder",
                options,
                "Select order"
            );


            fillSelect(
                "viewOrderItems",
                options,
                "Select order"
            );


            updatePickOrderDropdown();

        },

        null,

        "Could not load orders"

    );

}


/* =========================================================
   DISPLAY ORDERS
========================================================= */

function displayOrders() {

    renderTable(

        "ordersTableBody",

        state.orders,

        5,

        "No orders yet.",

        o => {

            const status =
                o.status ||
                "PENDING";


            return `

                <tr>

                    <td>
                        ${o.id ?? ""}
                    </td>

                    <td>
                        <strong>
                            ${escapeHtml(
                o.orderNumber
            )}
                        </strong>
                    </td>

                    <td>

                        <span class="
                            badge
                            badge-${status.toLowerCase()}
                        ">

                            ${escapeHtml(status)}

                        </span>

                    </td>

                    <td>
                        ${formatDate(
                o.createdAt
            )}
                    </td>

                    <td>

                        <button
                            class="edit-button"
                            onclick="editOrder(${o.id})">

                            Edit

                        </button>

                        <button
                            class="delete-button"
                            onclick="deleteOrder(${o.id})">

                            Delete

                        </button>

                    </td>

                </tr>

            `;

        }

    );

}


/* =========================================================
   CREATE ORDER
========================================================= */

async function createOrder(event) {

    event.preventDefault();


    const form =
        event.target;


    const orderNumber =
        $("orderNumber")
            .value
            .trim();


    if (!orderNumber) {

        return showMessage(
            "Order number is required"
        );

    }


    await run(

        async () => {

            await api(

                "/orders",

                {

                    method:
                        "POST",

                    body: {

                        orderNumber,

                        status:
                            "PENDING"

                    }

                }

            );


            form.reset();

            await loadOrders();

        },

        "Order created successfully",

        "Could not create order"

    );

}


/* =========================================================
   EDIT ORDER
========================================================= */

async function editOrder(id) {

    const order =
        state.orders.find(
            o =>
                Number(o.id) ===
                Number(id)
        );


    if (!order) return;


    const orderNumber =
        prompt(
            "Order number:",
            order.orderNumber
        );


    if (orderNumber === null) return;


    const status =
        prompt(
            "Status (PENDING / PICKING / COMPLETED):",
            order.status
        );


    if (status === null) return;


    await run(

        async () => {

            await api(

                `/orders/${id}`,

                {

                    method:
                        "PUT",

                    body: {

                        orderNumber:
                            orderNumber.trim(),

                        status:
                            status
                                .trim()
                                .toUpperCase()

                    }

                }

            );


            await loadOrders();

        },

        "Order updated successfully",

        "Could not update order"

    );

}


/* =========================================================
   DELETE ORDER
========================================================= */

async function deleteOrder(id) {

    if (
        !confirm(
            "Are you sure you want to delete this order?"
        )
    ) return;


    await run(

        async () => {

            await api(

                `/orders/${id}`,

                {
                    method:
                        "DELETE"
                }

            );


            await loadOrders();

        },

        "Order deleted successfully",

        "Could not delete order"

    );

}


/* =========================================================
   ORDER ITEMS
========================================================= */

async function addOrderItem(event) {

    event.preventDefault();


    const form =
        event.target;


    const orderId =
        Number(
            $("orderItemOrder")
                .value
        );


    const productId =
        Number(
            $("orderItemProduct")
                .value
        );


    const quantity =
        Number(
            $("orderItemQuantity")
                .value
        );


    if (
        !orderId ||
        !productId ||
        quantity <= 0
    ) {

        return showMessage(
            "Select an order, a product and a valid quantity"
        );

    }


    await run(

        async () => {

            await api(

                `/order-items?orderId=${orderId}&productId=${productId}&quantity=${quantity}`,

                {
                    method:
                        "POST"
                }

            );


            form.reset();


            $("viewOrderItems").value =
                orderId;


            await loadOrderItems(
                orderId
            );

        },

        "Order item added successfully",

        "Could not add order item"

    );

}


/* =========================================================
   LOAD ORDER ITEMS
========================================================= */

async function loadOrderItems(orderId) {

    await run(

        async () => {

            const result =
                await api(
                    `/order-items/order/${orderId}`
                );


            displayOrderItems(
                toList(result)
            );

        },

        null,

        "Could not load order items"

    );

}


/* =========================================================
   DISPLAY ORDER ITEMS
========================================================= */

function displayOrderItems(items) {

    renderTable(

        "orderItemsTableBody",

        items,

        5,

        "No order items found.",

        i => `

            <tr>

                <td>
                    ${i.id ?? ""}
                </td>

                <td>
                    ${escapeHtml(
            i.product?.name ||
            "Unknown"
        )}
                </td>

                <td>
                    ${escapeHtml(
            i.product?.sku ||
            "-"
        )}
                </td>

                <td>
                    ${i.quantity ?? 0}
                </td>

                <td>

                    <button
                        class="delete-button"
                        onclick="deleteOrderItem(${i.id})">

                        Delete

                    </button>

                </td>

            </tr>

        `

    );

}


/* =========================================================
   DELETE ORDER ITEM
========================================================= */

async function deleteOrderItem(id) {

    if (
        !confirm(
            "Delete this order item?"
        )
    ) return;


    await run(

        async () => {

            await api(

                `/order-items/${id}`,

                {
                    method:
                        "DELETE"
                }

            );


            const orderId =
                $("viewOrderItems")
                    ?.value;


            if (orderId) {

                await loadOrderItems(
                    orderId
                );

            }

        },

        "Order item deleted",

        "Could not delete order item"

    );

}


/* =========================================================
   PICK LISTS
========================================================= */

async function loadPickLists() {

    await run(

        async () => {

            state.pickLists =
                toList(
                    await api(
                        "/pick-lists"
                    )
                );


            setText(
                "pickListCount",
                state.pickLists.length
            );


            displayPickLists();


            updatePickOrderDropdown();

        },

        null,

        "Could not load pick lists"

    );

}


/* =========================================================
   FIND PICK LIST
========================================================= */

function findPickListForOrder(
    orderId
) {

    if (!orderId) return null;


    return state.pickLists.find(

        p =>

            p &&

            (

                Number(
                    p.order?.id
                ) ===
                Number(orderId)

                ||

                (
                    p.orderId !== undefined &&

                    Number(
                        p.orderId
                    ) ===
                    Number(orderId)
                )

            )

    ) || null;

}


/* =========================================================
   PICK ORDER DROPDOWN
========================================================= */

function updatePickOrderDropdown() {

    const options =
        state.orders.map(o => ({

            value:
            o.id,

            text:

                findPickListForOrder(o.id)

                    ? `${o.orderNumber} - Pick list already exists`

                    : `${o.orderNumber} - ${o.status}`

        }));


    fillSelect(
        "pickOrder",
        options,
        "Select order"
    );

}


/* =========================================================
   DISPLAY PICK LISTS
========================================================= */

function displayPickLists() {

    renderTable(

        "pickListsTableBody",

        state.pickLists,

        5,

        "No pick lists yet.",

        p => {

            const status =
                p.status ||
                "READY";


            const done =
                status.toUpperCase() ===
                "COMPLETED";


            const orderLabel =

                p.order?.orderNumber ||

                p.orderNumber ||

                (
                    p.orderId
                        ? "Order #" +
                        p.orderId
                        : "Unknown"
                );


            return `

                <tr>

                    <td>
                        ${p.id ?? ""}
                    </td>

                    <td>
                        <strong>
                            ${escapeHtml(
                orderLabel
            )}
                        </strong>
                    </td>

                    <td>

                        <span class="
                            badge
                            badge-${status.toLowerCase()}
                        ">

                            ${escapeHtml(
                status
            )}

                        </span>

                    </td>

                    <td>
                        ${formatDate(
                p.createdAt
            )}
                    </td>

                    <td>

                        <button
                            class="view-button"
                            onclick="viewPickList(${p.id})">

                            View

                        </button>

                        ${
                done

                    ?

                    `<span class="completed-label">
                                    Completed
                                </span>`

                    :

                    `

                                <button
                                    class="complete-button"
                                    onclick="completePickList(${p.id})">

                                    Complete

                                </button>

                                <button
                                    class="delete-button"
                                    onclick="deletePickList(${p.id})">

                                    Delete

                                </button>

                                `
            }

                    </td>

                </tr>

            `;

        }

    );

}


/* =========================================================
   GENERATE PICK LIST
========================================================= */

async function generatePickList(event) {

    event.preventDefault();


    const orderId =
        Number(
            $("pickOrder")
                .value
        );


    if (!orderId) {

        return showMessage(
            "Please select an order"
        );

    }


    const existing =
        findPickListForOrder(
            orderId
        );


    if (existing) {

        showMessage(
            `A pick list already exists for this order (ID ${existing.id})`
        );


        return loadPickListItems(
            existing.id
        );

    }


    const button =
        document.querySelector(
            "#pickListForm button[type='submit']"
        );


    button.disabled = true;

    button.innerHTML =
        "⏳ Generating...";


    try {

        const pickList =
            await api(

                `/pick-lists/generate/${orderId}`,

                {
                    method:
                        "POST"
                }

            );


        showMessage(
            "Optimized pick list generated successfully"
        );


        await Promise.all([

            loadPickLists(),

            loadOrders()

        ]);


        if (pickList?.id) {

            await loadPickListItems(
                pickList.id
            );

        }

    } catch (error) {

        const message =
            error.message.toLowerCase();


        const duplicate =

            error.status === 409 ||

            message.includes(
                "duplicate"
            ) ||

            message.includes(
                "already exists"
            ) ||

            message.includes(
                "unique constraint"
            );


        if (duplicate) {

            showMessage(
                "A pick list already exists for this order."
            );


            await loadPickLists();


            const found =
                findPickListForOrder(
                    orderId
                );


            if (found) {

                await loadPickListItems(
                    found.id
                );

            }

        } else {

            console.error(error);

            showMessage(
                "Could not generate pick list: " +
                error.message
            );

        }

    } finally {

        button.disabled = false;

        button.innerHTML =
            "⚡ Generate Pick List";

    }

}


/* =========================================================
   VIEW PICK LIST
========================================================= */

const viewPickList =
    id =>
        loadPickListItems(id);


/* =========================================================
   LOAD PICK LIST ITEMS
========================================================= */

async function loadPickListItems(id) {

    await run(

        async () => {

            const result =
                await api(
                    `/pick-lists/${id}/items`
                );


            displayPickListItems(
                toList(result)
            );

        },

        null,

        "Could not load pick list items"

    );

}


/* =========================================================
   DISPLAY PICK LIST ITEMS
========================================================= */

function displayPickListItems(items) {

    const sorted =
        [...items].sort(

            (a, b) =>

                Number(
                    a.pickSequence || 0
                )

                -

                Number(
                    b.pickSequence || 0
                )

        );


    renderTable(

        "pickListTableBody",

        sorted,

        5,

        "No pick list items found.",

        i => `

            <tr>

                <td>

                    <span class="workflow-icon teal"
                          style="
                            width:28px;
                            height:28px;
                            margin:0;
                            display:inline-grid;
                          ">

                        ${i.pickSequence ?? "-"}

                    </span>

                </td>

                <td>
                    ${escapeHtml(
            i.product?.name ||
            "Unknown"
        )}
                </td>

                <td>

                    <strong>
                        ${escapeHtml(
            i.bin?.binCode ||
            "Unknown"
        )}
                    </strong>

                </td>

                <td>
                    ${i.bin?.distanceFromDispatch ?? "-"}
                </td>

                <td>
                    ${i.quantity ?? "-"}
                </td>

            </tr>

        `

    );

}


/* =========================================================
   COMPLETE PICK LIST
========================================================= */

async function completePickList(id) {

    const pickList =
        state.pickLists.find(
            p =>
                Number(p.id) ===
                Number(id)
        );


    if (
        String(
            pickList?.status
        ).toUpperCase() ===
        "COMPLETED"
    ) {

        return showMessage(
            "This pick list is already completed."
        );

    }


    if (
        !confirm(

            "Complete this pick list?\n\n" +

            "• Stock quantity will be reduced\n" +

            "• Bin quantity will be reduced\n" +

            "• Order will be marked COMPLETED\n" +

            "• Pick list will be marked COMPLETED"

        )
    ) return;


    await run(

        async () => {

            await api(

                `/pick-lists/${id}/complete`,

                {
                    method:
                        "PUT"
                }

            );


            await refreshAllData();

        },

        "Pick list completed. Stock updated.",

        "Could not complete pick list"

    );

}


/* =========================================================
   DELETE PICK LIST
========================================================= */

async function deletePickList(id) {

    if (
        !confirm(
            "Delete this pick list?\n\nYou can generate a new one afterwards."
        )
    ) return;


    await run(

        async () => {

            await api(

                `/pick-lists/${id}`,

                {
                    method:
                        "DELETE"
                }

            );


            await Promise.all([

                loadPickLists(),

                loadOrders()

            ]);

        },

        "Pick list deleted successfully",

        "Could not delete pick list"

    );

}


/* =========================================================
   FORM SETUP
========================================================= */

function setupForms() {

    $("productForm")
        ?.addEventListener(
            "submit",
            addProduct
        );


    $("binForm")
        ?.addEventListener(
            "submit",
            addBin
        );


    $("stockForm")
        ?.addEventListener(
            "submit",
            receiveStock
        );


    $("orderForm")
        ?.addEventListener(
            "submit",
            createOrder
        );


    $("orderItemForm")
        ?.addEventListener(
            "submit",
            addOrderItem
        );


    $("pickListForm")
        ?.addEventListener(
            "submit",
            generatePickList
        );


    $("viewOrderItems")
        ?.addEventListener(
            "change",
            function () {

                const orderId =
                    Number(this.value);


                if (orderId) {

                    loadOrderItems(
                        orderId
                    );

                } else {

                    renderTable(

                        "orderItemsTableBody",

                        [],

                        5,

                        "Select an order to view its items.",

                        () => ""

                    );

                }

            }
        );


    $("pickOrder")
        ?.addEventListener(
            "change",
            function () {

                const existing =
                    findPickListForOrder(
                        Number(this.value)
                    );


                if (existing) {

                    showMessage(
                        `A pick list already exists: #${existing.id}`
                    );

                }

            }
        );

}


/* =========================================================
   REFRESH EVERYTHING
========================================================= */

async function refreshAllData() {

    await Promise.all([

        loadProducts(),

        loadBins(),

        loadStocks(),

        loadOrders(),

        loadPickLists()

    ]);

}


/* =========================================================
   GLOBAL REFRESH
========================================================= */

window.refreshWarehouseData =
    refreshAllData;


/* =========================================================
   START APPLICATION
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    () => {

        setupForms();

        refreshAllData();

    }
);